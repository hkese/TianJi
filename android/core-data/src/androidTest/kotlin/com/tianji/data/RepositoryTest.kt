package com.tianji.data
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tianji.domain.*
import com.tianji.qimen.*
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class) class RepositoryTest {
 @Test fun persistedJourneyBackupAndDelete():Unit=runBlocking{
  val context=InstrumentationRegistry.getInstrumentation().targetContext;context.deleteDatabase("tianji.db");var repo=Repository.open(context)
  val g=Goal(title="A practical step",successCriteria="One page");repo.saveGoal(g);repo.saveMilestone(Milestone(goalId=g.id,title="First page"));val p=repo.newPlan(g.id,"Asia/Hong_Kong","initial");val a=repo.snapshot().actions.first{it.planVersionId==p.id};repo.choose(a);repo.journal(JournalEntry(actionId=a.id,outcome="SUCCESS",score=8));repo.saveGoal(g.copy(title="Updated title"));assertEquals(3,repo.snapshot().actions.size);val next=repo.newPlan(g.id,"Asia/Hong_Kong","New observation");assertEquals(p.id,next.parentVersionId);val backup=repo.export();repo.db.close();repo=Repository.open(context);assertEquals(2,repo.snapshot().plans.size);assertEquals(1,repo.snapshot().journal.size);repo.deleteAll();assertTrue(repo.snapshot().goals.isEmpty());repo.restore(backup);assertEquals(backup,repo.export());try{repo.restore(backup.replace("HEURISTIC","VALIDATED"));fail("Invalid backup accepted")}catch(_:IllegalArgumentException){};assertEquals(backup,repo.export());repo.deleteGoal(g.id);assertTrue(repo.snapshot().journal.isEmpty());repo.db.close()
 }
 @Test fun legacyMigrationPreservesRows():Unit=runBlocking{
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  context.deleteDatabase("tianji.db")
  val old=context.openOrCreateDatabase("tianji.db",0,null)
  val ddl=InstrumentationRegistry.getInstrumentation().context.assets.open("original-v1.sql").bufferedReader().readText()
  ddl.split(";").filter{it.isNotBlank()}.forEach{old.execSQL(it)}
  val id=uid();val payload="""{"id":"$id","title":"Legacy goal","description":"","createdAt":"2026-01-01T00:00:00Z","deadline":null,"successCriteria":"One page","status":"ACTIVE","priority":2,"constraints":{"maxMinutes":25,"budget":0.0,"notes":""}}"""
  old.execSQL("INSERT INTO Goal(id,title,createdAt,deadline,status,priority,payload) VALUES(?,?,?,?,?,?,?)",arrayOf(id,"Legacy goal","2026-01-01T00:00:00Z",null,"ACTIVE",2,payload));old.version=1;old.close()
  val repo=Repository.open(context);val goal=repo.snapshot().goals.single();assertEquals(id,goal.id);assertEquals(25,goal.constraints.minutes);repo.newPlan(id,"UTC","After migration");assertEquals(3,repo.snapshot().actions.size);repo.db.close()
 }
 @Test fun chartSaveDoesNotDeleteLinkedActionsAndLegacyBackupStillRestores():Unit=runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  context.deleteDatabase("tianji.db");val repo=Repository.open(context)
  val g=Goal(title="Chart persistence",successCriteria="Keep history");repo.saveGoal(g)
  repo.newPlan(g.id,"Asia/Hong_Kong","test")
  val snapshot=repo.snapshot().charts.single()
  val chart=Canonical.decodeFromString<Chart>(snapshot.chartJson)
  repo.saveChart(chart);repo.saveChart(chart)
  assertEquals(3,repo.snapshot().actions.size)
  val publicChart=QimenCore().calculateChart(Instant.parse("2027-02-06T02:00:00Z"),"Asia/Hong_Kong")
  repo.saveChart(publicChart)
  val legacy=UnverifiedShiJia.calculate(Input("2024-02-29T23:00:00Z","Asia/Hong_Kong"))
  val old=ChartSnapshot(legacy.chart_id,legacy.input.instant_utc,legacy.input.timezone,legacy.ruleset.id,legacy.ruleset.version,Canonical.encodeToString(Chart.serializer(),legacy),legacy.input_hash)
  val backup=Codec.encodeToString(Backup.serializer(),repo.snapshot().let{it.copy(charts=it.charts+old)})
  repo.restore(backup);assertEquals(backup,repo.export())
  assertEquals(3,repo.snapshot().charts.size)
  try{repo.restore(backup.replace("天柱","天蓬"));fail("Tampered symbols accepted")}catch(_:IllegalArgumentException){}
  assertEquals(backup,repo.export());repo.db.close()
 }
 @Test fun credentialRoundTrip(){val context=InstrumentationRegistry.getInstrumentation().targetContext;val vault=CredentialVault(context);vault.save("test-only-not-a-real-key");assertEquals("test-only-not-a-real-key",vault.read());vault.clear();assertEquals("",vault.read())}
}
