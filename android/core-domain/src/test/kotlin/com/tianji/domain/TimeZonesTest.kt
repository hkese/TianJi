package com.tianji.domain

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class TimeZonesTest {
    @Test fun seattleUsesPacificDaylightRulesNotAFixedOffset() {
        val zone = TimeZones.common.single { TimeZones.matches(it, "Seattle") }
        assertEquals("America/Los_Angeles", zone.id)
        assertEquals("UTC-08:00", TimeZones.offset(zone.id, Instant.parse("2026-01-15T12:00:00Z")))
        assertEquals("UTC-07:00", TimeZones.offset(zone.id, Instant.parse("2026-07-15T12:00:00Z")))
        assertEquals("UTC-08:00", TimeZones.offset(zone.id, Instant.parse("2026-03-08T09:59:59Z")))
        assertEquals("UTC-07:00", TimeZones.offset(zone.id, Instant.parse("2026-03-08T10:00:00Z")))
    }

    @Test fun chineseAndEnglishSearchFindTheSameCity() {
        for (q in listOf("西雅圖", "西雅图", " seattle ", "RENTON", "Los Angeles")) {
            assertEquals("America/Los_Angeles", TimeZones.common.single { TimeZones.matches(it, q) }.id)
        }
    }

    @Test fun hongKongAndUtcHaveStableOffsets() {
        for (date in listOf("2026-01-01T00:00:00Z", "2026-07-01T00:00:00Z")) {
            assertEquals("UTC+08:00", TimeZones.offset("Asia/Hong_Kong", Instant.parse(date)))
            assertEquals("UTC+00:00", TimeZones.offset("UTC", Instant.parse(date)))
        }
    }

    @Test fun everyPresetIsAValidDistinctZone() {
        assertEquals(TimeZones.common.size, TimeZones.common.map { it.id }.distinct().size)
        TimeZones.common.forEach { ZoneId.of(it.id) }
    }
}
