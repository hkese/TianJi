package com.tianji.domain
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
class PlannerTest {
 private val now=Instant.parse("2026-10-01T00:00:00Z")
 private fun chart()=ChartSnapshot(uid(),now.toString(),"Asia/Hong_Kong","blocked","0","{}","hash")
 private fun goal()=Goal(title="Read one page",successCriteria="One page read")
 @Test fun threeBoundedAlternatives(){val g=goal();val p=PlanVersion(goalId=g.id,reason="initial");val a=Planner.candidates(g,p,chart(),now);assertEquals(3,a.size);assertEquals(3,a.map{it.kind}.distinct().size);a.forEach{assertEquals("HEURISTIC",it.evidenceLabel);assertEquals(0.0,it.cost,0.0);assertTrue(Instant.parse(it.plannedEnd)<=now.plusSeconds(1200));assertEquals(p.id,it.planVersionId)}}
 @Test fun deadlineBoundsAllAlternatives(){val g=goal().copy(deadline=now.plusSeconds(420).toString());val a=Planner.candidates(g,PlanVersion(goalId=g.id,reason="x"),chart(),now);assertTrue(a.all{it.plannedEnd==g.deadline})}
 @Test(expected=IllegalArgumentException::class) fun pastDeadlineRejected(){val g=goal().copy(deadline=now.minusSeconds(1).toString());Planner.candidates(g,PlanVersion(goalId=g.id,reason="x"),chart(),now)}
 @Test(expected=IllegalArgumentException::class) fun invalidCostRejected(){validateGoal(goal().copy(constraints=Constraints(maxCost=Double.NaN)))}
 @Test(expected=IllegalArgumentException::class) fun orphanJournalRejected(){Backup(journal=listOf(JournalEntry(actionId=uid(),outcome="UNKNOWN"))).validate()}
 @Test fun fullBackupRoundTrip(){val g=goal();val c=chart();val p=PlanVersion(goalId=g.id,reason="initial");val actions=Planner.candidates(g,p,c,now);val b=Backup(goals=listOf(g),charts=listOf(c),plans=listOf(p),actions=actions,journal=listOf(JournalEntry(actionId=actions[0].id,outcome="UNKNOWN")));b.validate();val json=kotlinx.serialization.json.Json.encodeToString(Backup.serializer(),b);assertEquals(b,Codec.decodeFromString<Backup>(json))}
 @Test(expected=IllegalArgumentException::class) fun cyclicHistoryRejected(){val g=goal();val one=uid();val two=uid();Backup(goals=listOf(g),plans=listOf(PlanVersion(one,g.id,now.toString(),two,"a"),PlanVersion(two,g.id,now.toString(),one,"b"))).validate()}
}
