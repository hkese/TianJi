package com.tianji.data
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tianji.domain.*
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
 @Test fun credentialRoundTrip(){val context=InstrumentationRegistry.getInstrumentation().targetContext;val vault=CredentialVault(context);vault.save("test-only-not-a-real-key");assertEquals("test-only-not-a-real-key",vault.read());vault.clear();assertEquals("",vault.read())}
}
