package com.tianji.qimen
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
import java.io.File
class CoreTest {
 private val core=QimenCore()
 @Test fun deterministicBlockedOutput(){val i=Instant.parse("2024-02-29T23:00:00Z");assertEquals(core.calculateChart(i,"Asia/Hong_Kong"),QimenCore().calculateChart(i,"Asia/Hong_Kong"));val c=core.calculateChart(i,"Asia/Hong_Kong");assertEquals("BLOCKED",c.status);assertTrue(c.palaces.isEmpty());assertNull(c.ju_number);assertNull(c.dun_type)}
 @Test fun civilBoundaryInputsRemainHonest(){listOf("2024-02-29T23:59:59Z","2024-03-01T00:00:00Z","2024-03-10T06:59:59Z","2024-03-10T07:00:00Z","2024-11-03T06:00:00Z").forEach{val c=core.calculateChart(Instant.parse(it),"America/New_York");assertEquals("BLOCKED",c.status);assertEquals(64,c.input_hash.length)}}
 @Test fun timezoneIsPartOfInputHash(){val i=Instant.EPOCH;assertNotEquals(core.calculateChart(i,"UTC").input_hash,core.calculateChart(i,"Asia/Hong_Kong").input_hash)}
 @Test(expected=Exception::class) fun invalidZoneRejected(){core.calculateChart(Instant.EPOCH,"Invalid/Zone")}
 @Test(expected=IllegalArgumentException::class) fun unpairedCoordinatesRejected(){core.calculateChart(Instant.EPOCH,"UTC",latitude=10.0)}
 @Test(expected=IllegalArgumentException::class) fun invalidLatitudeRejected(){core.calculateChart(Instant.EPOCH,"UTC",latitude=100.0,longitude=0.0)}
 @Test fun batchEqualsReplay(){val times=listOf(Instant.EPOCH,Instant.EPOCH.plusSeconds(86400));assertEquals(times.map{core.calculateChart(it,"UTC")},core.batch(times,"UTC"))}
 @Test fun crossLanguageFixture(){val c=core.calculateChart(Instant.parse("2024-02-29T23:00:00Z"),"Asia/Hong_Kong");val file=File(System.getProperty("tianji.contractFixture"));file.parentFile.mkdirs();file.writeText(core.canonical(c));assertEquals(c,Canonical.decodeFromString<Chart>(file.readText()))}
}
