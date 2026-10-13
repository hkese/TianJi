package com.tianji.qimen

import org.junit.Test
import org.junit.Assert.*
import java.time.*

class DirectionRulesTest {
    private val core=QimenCore()
    @Test fun classicalJadeMaidenWorkedExampleAgreesWithCoreGeometry(){
        // R7 vol.2: Yang 1, Jia/Ji day, Geng-Wu hour: duty Rest door meets earth Ding at Dui 7.
        // Gregorian date is found by the tested calendar, not claimed as an independent ephemeris fixture.
        val dates=(0L..365L).map{LocalDate.of(2026,1,1).plusDays(it)}
        val c=dates.map{core.calculateChart(it.atTime(12,0).toInstant(ZoneOffset.ofHours(8)),"Asia/Shanghai")}.first{
            it.dun_type=="陽遁" && it.ju_number==1 && it.calendar["hour_ganzhi"]=="庚午" && it.calendar.getValue("day_ganzhi").first() in "甲己"
        }
        println("R7_WORKED_EXAMPLE "+c.input.instant_utc+" matches="+DirectionRules.matches(c,"HARMONY"));assertEquals("7",c.calendar["duty_door_palace"])
        val p=c.palaces.single{it.index==7};assertEquals("休門",p.door);assertEquals("丁",p.earth_stem)
        assertTrue(DirectionRules.decisions(c,"HARMONY").single{it.palace==7}.matched.contains("R7_YUNU_SHOUMEN"))
    }
    @Test fun windowsSplitDstGapOverlapAndSolarTermWithoutChangingChartInside(){
        for((at,zone) in listOf("2026-03-08T07:30:00Z" to "America/Los_Angeles","2026-11-01T07:30:00Z" to "America/Los_Angeles","2026-10-08T04:00:00Z" to "Asia/Hong_Kong")){
            val start=Instant.parse(at);val end=start.plusSeconds(36*3600)
            val windows=DirectionRules.windows(core,start,end,zone)
            assertEquals(start,windows.first().start);assertEquals(end,windows.last().end)
            assertTrue(windows.zipWithNext().all{(a,b)->a.end==b.start})
            windows.forEach{w->assertTrue(w.end>w.start);val last=core.calculateChart(w.end.minusNanos(1),zone)
                assertEquals(w.chart.palaces,last.palaces);assertEquals(w.chart.calendar["day_ganzhi"],last.calendar["day_ganzhi"]);assertEquals(w.chart.calendar["hour_ganzhi"],last.calendar["hour_ganzhi"])
            }
        }
    }
    @Test fun matchesAlwaysHaveDocumentedConjunctionAndNoKnownExclusion(){
        var count=0
        for(d in 0L..59L)for(h in 0L..22L step 2){
            val c=core.calculateChart(Instant.parse("2026-01-01T00:00:00Z").plusSeconds(d*86400+h*3600),"Asia/Hong_Kong")
            for(purpose in DirectionRules.purposes)for(m in DirectionRules.matches(c,purpose)){
                val p=c.palaces.single{it.index==m.palace};count++
                assertTrue(DirectionRules.decisions(c,purpose).single{it.palace==m.palace}.excluded.isEmpty())
                if(purpose=="OUTING"){assertTrue(p.door in listOf("開門","休門","生門"));assertTrue(p.heaven_stem!!.substringBefore("、") in listOf("乙","丙","丁"))}
                else assertTrue(m.ruleIds.all{it in listOf("R7_REN_DUN","R7_YUNU_SHOUMEN")})
            }
        }
        assertTrue("Selection must have actual matches, not an always-empty gate",count>10)
    }
    @Test(expected=IllegalArgumentException::class) fun blockedChartsCannotBeUsed(){DirectionRules.matches(QimenCore(listOf(UnverifiedShiJia)).calculateChart(Instant.parse("2026-01-01T00:00:00Z"),"UTC"),"OUTING")}
    @Test fun unknownPurposeCannotSilentlyFallBack(){assertTrue(runCatching{DirectionRules.matches(core.calculateChart(Instant.parse("2026-01-01T00:00:00Z"),"UTC"),"UNKNOWN")}.isFailure)}
}
