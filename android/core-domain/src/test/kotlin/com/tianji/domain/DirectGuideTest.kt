package com.tianji.domain

import com.tianji.qimen.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class DirectGuideTest {
 private val now=Instant.parse("2026-10-13T23:41:00Z")
 private fun candidate():Pair<ActionCandidate,Chart> {
  val goal=Intention.goal("Synthetic outing",now,"America/Los_Angeles")
  val p=DirectionNavigator.plan(goal,PlanVersion(goalId=goal.id,createdAt=now.toString(),reason="synthetic",entryMode="INTENTION"),DirectionRequest(horizonHours=168),now,"America/Los_Angeles")
  val a=p.actions.first();return a to Codec.decodeFromString<Chart>(p.charts.single{it.id==a.chartId}.chartJson)
 }
 @Test fun neverSaysDepartNowBeforeWindowAndExpiresAtExclusiveEnd(){
  val(a,c)=candidate();val start=Instant.parse(a.plannedStart);val end=Instant.parse(a.plannedEnd)
  assertEquals("WAIT",DirectGuide.from(a,c,start.minusNanos(1)).state)
  assertEquals("READY",DirectGuide.from(a,c,start).state)
  assertEquals("READY",DirectGuide.from(a,c,end.minusNanos(1)).state)
  val expired=DirectGuide.from(a,c,end);assertEquals("EXPIRED",expired.state);assertTrue(expired.avoid.isEmpty())
 }
 @Test fun existingJourneyContinuesAfterDepartureWindowWithoutNewDeparture(){
  val(a,c)=candidate();val start=Instant.parse(a.plannedStart);val later=Instant.parse(a.plannedEnd).plusSeconds(600)
  val active=a.copy(itinerary=a.itinerary!!.copy(phase=1,departedAt=start.toString()))
  val view=DirectGuide.from(active,c,later);assertEquals("ACTIVE",view.state);assertTrue(view.avoid.isEmpty())
  val done=active.copy(itinerary=active.itinerary!!.copy(phase=4,arrivedAt=start.toString(),returningAt=start.toString(),finishedAt=start.toString()))
  assertEquals("DONE",DirectGuide.from(done,c,later).state)
 }
 @Test fun avoidOnlyUsesExcludedPalacesFromThatActionsOwnChart(){
  val(a,c)=candidate();val guide=DirectGuide.from(a,c,Instant.parse(a.plannedStart))
  val expected=DirectionRules.decisions(c,a.itinerary!!.purpose).filter{it.excluded.isNotEmpty()}
  assertEquals(expected.map{it.palace},guide.avoid.map{it.palace});assertFalse(guide.avoid.any{it.palace==a.itinerary!!.palace})
  guide.avoid.forEach{v->assertEquals(c.palaces.single{it.index==v.palace}.direction,v.direction);assertEquals(expected.single{it.palace==v.palace}.excluded,v.reasons)}
  assertEquals(Navigator.bearing(a.itinerary!!.palace),guide.bearing)
 }
 @Test fun rejectsAnotherChartAndExcludedDirectionRatherThanRenderingForgedAdvice(){
  val(a,c)=candidate();assertTrue(runCatching{DirectGuide.from(a.copy(chartId="another-chart"),c,now)}.isFailure)
  val bad=DirectionRules.decisions(c,a.itinerary!!.purpose).first{it.excluded.isNotEmpty()}.palace
  assertTrue(runCatching{DirectGuide.from(a.copy(itinerary=a.itinerary!!.copy(palace=bad)),c,now)}.isFailure)
 }
 @Test fun oneTapDailyGoalStopsAtLocalMidnightWithoutBorrowingTomorrowsWindows(){
  assertEquals(DirectGuide.DAILY_INTENTION,DirectGuide.intention("  "));assertEquals("specific",DirectGuide.intention(" specific "))
  val goal=Intention.goal(DirectGuide.intention(""),now,"America/Los_Angeles")
  assertEquals("2026-10-14T07:00:00Z",goal.deadline)
  val p=DirectionNavigator.plan(goal,PlanVersion(goalId=goal.id,createdAt=now.toString(),reason="daily",entryMode="INTENTION"),Intention.request(goal.title),now,"America/Los_Angeles")
  assertTrue(p.actions.all{Instant.parse(it.plannedEnd)<=Instant.parse(goal.deadline).minusSeconds(1200)})
 }
 @Test fun readableWindowDisambiguatesDstAndLabelsTomorrowInItsChartZone(){
  val dst=DirectGuide.window(Instant.parse("2026-11-01T08:00:00Z"),Instant.parse("2026-11-01T09:00:00Z"),Instant.parse("2026-11-01T07:00:00Z"),"America/Los_Angeles",false)
  assertTrue(dst.contains("-07:00"));assertTrue(dst.contains("-08:00"));assertTrue(dst.contains("今日"))
  val next=DirectGuide.window(Instant.parse("2026-10-14T08:00:00Z"),Instant.parse("2026-10-14T10:00:00Z"),now,"America/Los_Angeles",false)
  assertTrue(next.contains("聽日"))
 }
}
