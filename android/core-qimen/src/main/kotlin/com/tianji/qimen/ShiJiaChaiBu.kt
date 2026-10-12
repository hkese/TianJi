package com.tianji.qimen

import com.nlf.calendar.Solar
import java.time.*
import java.util.UUID
import kotlinx.serialization.encodeToString

/** Single documented convention. R1–R5: docs/RULESET_REFERENCES.md. No Android or network. */
object ShiJiaChaiBu : QimenRuleset {
    override val metadata = Ruleset("shijia-zhuanpan-chaibu-civil-kun2", "1.0.0",
        "《煙波釣叟歌》；拆補轉盤・子初換日・中五寄坤二；lunar-java 1.7.7；docs/RULESET_REFERENCES.md")
    val ring = listOf(1, 8, 3, 4, 9, 2, 7, 6)
    private val stems = "甲乙丙丁戊己庚辛壬癸"
    private val branches = "子丑寅卯辰巳午未申酉戌亥"
    private val instruments = "戊己庚辛壬癸丁丙乙"
    private val stars = listOf("天蓬", "天任", "天沖", "天輔", "天英", "天芮", "天柱", "天心")
    private val doors = listOf("休門", "生門", "傷門", "杜門", "景門", "死門", "驚門", "開門")
    private val gods = listOf("值符", "螣蛇", "太陰", "六合", "白虎", "玄武", "九地", "九天")
    private val directions = listOf("北", "西南", "東", "東南", "中央", "西北", "西", "東北", "南")
    private val terms = listOf("冬至", "小寒", "大寒", "立春", "雨水", "驚蟄", "春分", "清明", "穀雨", "立夏", "小滿", "芒種", "夏至", "小暑", "大暑", "立秋", "處暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪")
    private val juTable = listOf("174", "285", "396", "852", "963", "174", "396", "417", "528", "417", "528", "639", "936", "825", "714", "258", "147", "936", "714", "693", "582", "693", "582", "471")
    private fun traditional(s: String) = s.replace("惊", "驚").replace("蛰", "蟄").replace("谷", "穀").replace("满", "滿").replace("种", "種").replace("处", "處")
    private fun gz(index: Int) = "${stems[Math.floorMod(index, 10)]}${branches[Math.floorMod(index, 12)]}"
    private fun host(p: Int) = if (p == 5) 2 else p
    private fun Solar.instant(): Instant = LocalDateTime.of(year, month, day, hour, minute, second).toInstant(ZoneOffset.ofHours(8))

    override fun calculate(input: Input): Chart {
        val instant = Instant.parse(input.instant_utc)
        val local = instant.atZone(ZoneId.of(input.timezone))
        require(local.year in 1900..2100) { "SUPPORTED_YEARS_1900_2100" }
        require((input.latitude == null) == (input.longitude == null))
        input.latitude?.let { require(it.isFinite() && it in -90.0..90.0) }
        input.longitude?.let { require(it.isFinite() && it in -180.0..180.0) }
        val normalized = input.copy(instant_utc = instant.toString())
        val hash = sha(Canonical.encodeToString(normalized) + "|" + metadata.id + "|" + metadata.version)

        // Solar terms are absolute instants, always read in the library's fixed UTC+8.
        // Civil day/time below deliberately use the user's IANA zone, including DST.
        val beijing = instant.atOffset(ZoneOffset.ofHours(8))
        val lunar = Solar.fromYmdHms(beijing.year, beijing.monthValue, beijing.dayOfMonth,
            beijing.hour, beijing.minute, beijing.second).lunar
        val term = lunar.getPrevJieQi(false)
        val nextTerm = lunar.getNextJieQi(false)
        val termName = traditional(term.name)
        val termIndex = terms.indexOf(termName)
        check(termIndex >= 0)
        val termStart = term.solar.instant()
        val termEnd = nextTerm.solar.instant()
        val qimenDate = local.toLocalDate().plusDays(if (local.hour >= 23) 1 else 0)
        val dayIndex = Math.floorMod(qimenDate.toEpochDay() - LocalDate.of(2000, 1, 1).toEpochDay() + 54, 60).toInt()
        val hourBranch = ((local.hour + 1) / 2) % 12
        val hourStem = ((dayIndex % 10 % 5) * 2 + hourBranch) % 10
        val hourIndex = (0..59).single { it % 10 == hourStem && it % 12 == hourBranch }
        val headIndex = dayIndex - dayIndex % 5
        val yuan = when (headIndex % 12 % 3) { 0 -> 0; 2 -> 1; else -> 2 }
        val yuanName = listOf("上元", "中元", "下元")[yuan]
        val yang = termIndex < 12
        val sign = if (yang) 1 else -1
        val dun = if (yang) "陽遁" else "陰遁"
        val ju = juTable[termIndex][yuan].digitToInt()
        val earth = mutableMapOf<Int, String>()
        instruments.forEachIndexed { i, s -> earth[Math.floorMod(ju - 1 + sign * i, 9) + 1] = s.toString() }
        val xunIndex = hourIndex / 10
        val hidden = instruments[xunIndex].toString()
        val originalDuty = earth.entries.single { it.value == hidden }.key
        val actualHourStem = if (hourStem == 0) hidden else stems[hourStem].toString()
        val originalTarget = earth.entries.single { it.value == actualHourStem }.key
        val dutyTarget = host(originalTarget)
        val from = ring.indexOf(host(originalDuty))
        val to = ring.indexOf(dutyTarget)
        val dutyStar = if (originalDuty == 5) "天禽" else stars[from]
        val dutyDoor = doors[from]
        // Count through all nine numeric palaces BEFORE applying center hosting.
        val originalDoorTarget = Math.floorMod(originalDuty - 1 + sign * (hourIndex % 10), 9) + 1
        val doorTarget = host(originalDoorTarget)
        val doorShift = ring.indexOf(doorTarget) - from
        val starShift = to - from
        val ruiPalace = ring[Math.floorMod(ring.indexOf(2) + starShift, 8)]
        val palaces = (1..9).map { p ->
            if (p == 5) Palace(p, directions[p - 1], null, null, null, null, earth.getValue(p))
            else {
                val pos = ring.indexOf(p)
                val sourcePos = Math.floorMod(pos - starShift, 8)
                val sourcePalace = ring[sourcePos]
                Palace(p, directions[p - 1], doors[Math.floorMod(pos - doorShift, 8)],
                    stars[sourcePos] + if (p == ruiPalace) "、天禽" else "",
                    gods[Math.floorMod((pos - to) * sign, 8)],
                    earth.getValue(sourcePalace) + if (p == ruiPalace) "、${earth.getValue(5)}" else "",
                    earth.getValue(p) + if (p == 2) "、${earth.getValue(5)}" else "")
            }
        }
        val nearTerm = Duration.between(termStart, instant).seconds < 120 || Duration.between(instant, termEnd).seconds < 120
        return Chart(chart_id = UUID.nameUUIDFromBytes(hash.toByteArray(Charsets.UTF_8)).toString(), status = "TRADITIONAL",
            input = normalized, ruleset = metadata, dun_type = dun, ju_number = ju, palaces = palaces,
            calendar = linkedMapOf(
                "civil_local_iso" to local.toString(), "day_boundary" to "23:00", "time_basis" to "CIVIL_IANA_NO_SOLAR_CORRECTION",
                "year_ganzhi" to lunar.yearInGanZhiExact, "month_ganzhi" to lunar.monthInGanZhiExact,
                "qimen_date" to qimenDate.toString(), "day_ganzhi" to gz(dayIndex), "hour_ganzhi" to gz(hourIndex),
                "solar_term" to termName, "solar_term_start_utc" to termStart.toString(), "next_solar_term_utc" to termEnd.toString(),
                "yuan" to yuanName, "fu_tou" to gz(headIndex), "xun_shou" to gz(xunIndex * 10), "hidden_stem" to hidden,
                "duty_origin" to originalDuty.toString(), "duty_star" to dutyStar, "duty_star_palace" to dutyTarget.toString(),
                "duty_star_raw_palace" to originalTarget.toString(), "duty_door" to dutyDoor, "duty_door_palace" to doorTarget.toString(),
                "duty_door_raw_palace" to originalDoorTarget.toString(), "center_heaven_host" to ruiPalace.toString(),
                "hour_focus_stem" to actualHourStem),
            derivation_steps = listOf(
                "民用時間 ${local.toLocalDate()} ${local.toLocalTime()}（${input.timezone}，${local.offset}）；不作真太陽時校正。[R4／固定口徑]",
                "當前節氣 $termName；交節以 UTC $termStart 判斷，年月柱為 ${lunar.yearInGanZhiExact}／${lunar.monthInGanZhiExact}。[R4]",
                "子初 23:00 換日：${gz(dayIndex)}日、${gz(hourIndex)}時；甲己符頭 ${gz(headIndex)}，取$yuanName。[R2]",
                "$termName$yuanName：$dun${ju}局；戊己庚辛壬癸丁丙乙由${ju}宮${if (yang) "順" else "逆"}排地盤。[R1／R2]",
                "${gz(xunIndex * 10)}旬，甲遁$hidden；旬首原在${originalDuty}宮，值符$dutyStar，值使$dutyDoor。[R1]",
                "值符加地盤時干$actualHourStem：原${originalTarget}宮，實寄${dutyTarget}宮；星及天盤干整體旋轉。[R1／R2]",
                "值使由原${originalDuty}宮${if (yang) "順" else "逆"}走${hourIndex % 10}步至${originalDoorTarget}宮，落五寄二，實落${doorTarget}宮；八門整體轉盤。[R1／R2]",
                "八神從值符${dutyTarget}宮${if (yang) "順" else "逆"}布；中五地盤干寄二，天禽與中干隨天芮至${ruiPalace}宮。[R2]"),
            warnings = listOf("NO_SCIENTIFIC_VALIDATION", "HUMAN_GOLDEN_TARGET_NOT_MET", "LOCATION_NOT_USED", "BASIC_INTERPRETATION_ONLY") +
                if (nearTerm) listOf("NEAR_SOLAR_TERM_BOUNDARY") else emptyList(), input_hash = hash)
    }
}

/** Explicit choices for ambiguous local times; no silent DST gap correction. */
object ChartTime {
    fun candidates(date: LocalDate, time: LocalTime, zone: ZoneId): List<Instant> {
        val local = date.atTime(time)
        return zone.rules.getValidOffsets(local).map { local.toInstant(it) }.sorted()
    }
}
