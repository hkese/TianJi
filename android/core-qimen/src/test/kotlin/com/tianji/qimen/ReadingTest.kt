package com.tianji.qimen
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test
class ReadingTest {
    @Test fun readingUsesActualHeavenStemIncludingHostedCenter() {
        val core=QimenCore();val seen=mutableSetOf<String>();var hosted=0
        val start=Instant.parse("2026-01-01T00:00:00Z")
        repeat(720) { i ->
            val c=core.calculateChart(start.plusSeconds(i*7200L),"Asia/Hong_Kong")
            val r=TraditionalReading.from(c)
            assertTrue(r.palace.heaven_stem!!.split("、").contains(r.stem))
            assertEquals(c.calendar["hour_focus_stem"],r.stem)
            assertTrue(r.meaningZh.contains(r.palace.door!!))
            assertFalse(r.meaningZh.contains("%"));assertFalse(r.stepZh.contains("必定"))
            seen+=r.palace.door!!
            if(r.palace.heaven_stem!!.substringAfter("、","")==r.stem)hosted++
        }
        assertEquals(8,seen.size);assertTrue(hosted>0)
    }
    @Test(expected=IllegalArgumentException::class) fun blockedChartsCannotBeInterpreted() {
        TraditionalReading.from(UnverifiedShiJia.calculate(Input("2026-01-01T00:00:00Z","UTC")))
    }
}
