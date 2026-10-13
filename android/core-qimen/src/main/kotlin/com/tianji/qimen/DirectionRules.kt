package com.tianji.qimen

import kotlinx.serialization.Serializable
import java.time.*

/** Explicit, limited classical selection profile. Never a probability or a full divination score. */
@Serializable data class DirectionMatch(val palace: Int, val ruleIds: List<String>, val reasons: List<String>)
@Serializable data class DirectionDecision(val palace: Int, val matched: List<String>, val excluded: List<String>)
data class DirectionWindow(val start: Instant, val end: Instant, val chart: Chart, val decisions: List<DirectionDecision>)
object DirectionRules {
    const val VERSION = "classical-direction-civil/1.0"
    val purposes = setOf("OUTING", "HARMONY")
    private val nativeStars = mapOf(1 to "天蓬",8 to "天任",3 to "天沖",4 to "天輔",9 to "天英",2 to "天芮",7 to "天柱",6 to "天心")
    private val nativeDoors = mapOf(1 to "休門",8 to "生門",3 to "傷門",4 to "杜門",9 to "景門",2 to "死門",7 to "驚門",6 to "開門")
    // Wood, fire, earth, metal, water; controlling relation is +2 modulo 5.
    private val elements = mapOf(1 to 4,2 to 2,3 to 0,4 to 0,6 to 3,7 to 3,8 to 2,9 to 1)
    private val doors = mapOf("休門" to 4,"生門" to 2,"傷門" to 0,"杜門" to 0,"景門" to 1,"死門" to 2,"驚門" to 3,"開門" to 3)
    private val punishment = mapOf("戊" to 3,"己" to 2,"庚" to 8,"辛" to 9,"壬" to 4,"癸" to 4)
    fun decisions(chart: Chart, purpose: String): List<DirectionDecision> {
        require(purpose in purposes)
        require(chart.status == "TRADITIONAL" && chart.ruleset == ShiJiaChaiBu.metadata)
        val outer = chart.palaces.filter { it.index != 5 }
        require(outer.size == 8)
        val stemOrder = "甲乙丙丁戊己庚辛壬癸"
        val day = stemOrder.indexOf(chart.calendar.getValue("day_ganzhi").first())
        val hour = stemOrder.indexOf(chart.calendar.getValue("hour_ganzhi").first())
        val global = buildList {
            if (hour == (day + 6) % 10) add("五不遇時")
            if (outer.all { it.star?.substringBefore("、") == nativeStars[it.index] }) add("九星伏吟")
            if (outer.all { it.star?.substringBefore("、") == nativeStars[10-it.index] }) add("九星反吟")
            if (outer.all { it.door == nativeDoors[it.index] }) add("八門伏吟")
            if (outer.all { it.door == nativeDoors[10-it.index] }) add("八門反吟")
            if ("NEAR_SOLAR_TERM_BOUNDARY" in chart.warnings) add("交節前後兩分鐘")
        }
        return outer.map { p ->
            // Hosted central stems are displayed but are not silently promoted to the main stem.
            val heaven = p.heaven_stem?.substringBefore("、")
            val earth = p.earth_stem?.substringBefore("、")
            val matched = buildList {
                if (purpose == "OUTING" && heaven in listOf("乙","丙","丁") && p.door in listOf("開門","休門","生門")) add("R1_QIMEN_HE")
                if (purpose == "HARMONY" && heaven == "丁" && p.door == "休門" && p.deity == "太陰") add("R7_REN_DUN")
                if (purpose == "HARMONY" && chart.calendar["duty_door_palace"] == p.index.toString() && earth == "丁") add("R7_YUNU_SHOUMEN")
            }
            val excluded = global + buildList {
                if ((doors.getValue(p.door!!) + 2) % 5 == elements.getValue(p.index)) add("門迫")
                if ((heaven in listOf("乙","丙") && p.index == 6) || (heaven == "丁" && p.index == 8)) add("三奇入墓")
                if (punishment[heaven] == p.index) add("六儀擊刑")
                if (p.star?.substringBefore("、") in listOf("天蓬","天芮")) add("本簡式避用蓬芮")
            }
            DirectionDecision(p.index,matched,excluded)
        }
    }
    fun name(id: String) = when(id) { "R1_QIMEN_HE" -> "奇門相合"; "R7_REN_DUN" -> "人遁"; "R7_YUNU_SHOUMEN" -> "玉女守門"; else -> error("Unknown direction rule") }
    fun reason(id: String) = when(id) {
        "R1_QIMEN_HE" -> "天盤主干乙／丙／丁，配開／休／生門。[R1]"
        "R7_REN_DUN" -> "天盤主干丁、休門與太陰同宮。[R7 卷二・人遁]"
        "R7_YUNU_SHOUMEN" -> "值使門到地盤主干丁所在宮。[R7 卷二・玉女守門]"
        else -> error("Unknown direction rule")
    }
    fun matches(chart: Chart, purpose: String) = decisions(chart,purpose).filter { it.matched.isNotEmpty() && it.excluded.isEmpty() }.map {
        DirectionMatch(it.palace,it.matched,it.matched.map(::reason))
    }
    /** Half-open windows, split at civil odd hours, zone transitions and exact solar-term boundaries.
     * Also split the 120-second exclusion margins; never bridge an excluded term boundary. */
    fun windows(core: QimenCore, start: Instant, end: Instant, timezone: String): List<DirectionWindow> {
        require(end > start && Duration.between(start,end) <= Duration.ofDays(7))
        val zone=ZoneId.of(timezone)
        val bounds=sortedSetOf(start,end)
        var date=start.atZone(zone).toLocalDate().minusDays(1)
        val last=end.atZone(zone).toLocalDate().plusDays(1)
        while(date<=last) {
            for(h in 1..23 step 2) ChartTime.candidates(date,LocalTime.of(h,0),zone).filter { it>start && it<end }.forEach(bounds::add)
            date=date.plusDays(1)
        }
        var transition=zone.rules.nextTransition(start.minusNanos(1))
        while(transition!=null && transition.instant<end) {
            if(transition.instant>start)bounds.add(transition.instant)
            transition=zone.rules.nextTransition(transition.instant)
        }
        // At most two terms within seven days; inspect both ends to include nearby margin cuts.
        listOf(start,end.minusNanos(1)).forEach { at ->
            val c=core.calculateChart(at,timezone)
            listOf("solar_term_start_utc","next_solar_term_utc").forEach { key ->
                val term=Instant.parse(c.calendar.getValue(key))
                listOf(term.minusSeconds(120),term,term.plusSeconds(120)).filter { it>start && it<end }.forEach(bounds::add)
            }
        }
        return bounds.toList().zipWithNext().map { (s,e) -> DirectionWindow(s,e,core.calculateChart(s,timezone),emptyList()) }
    }
}
