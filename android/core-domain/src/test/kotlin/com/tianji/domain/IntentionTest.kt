package com.tianji.domain

import java.time.Instant
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class IntentionTest {
 private val now=Instant.parse("2026-10-13T23:41:00Z")
 private fun plan(text:String="Synthetic outdoor intention",hours:Int=168):DirectionPlanResult {
  val g=Intention.goal(text,now,"America/Los_Angeles")
  return DirectionNavigator.plan(g,PlanVersion(goalId=g.id,createdAt=now.toString(),reason="synthetic",entryMode="INTENTION"),Intention.request(text,hours),now,"America/Los_Angeles",Intention.timing(text,now,"America/Los_Angeles").first)
 }
 @Test fun automaticRoutingKeepsClassicalRulesSeparate(){
  assertEquals("HARMONY",Intention.request("希望相處和氣").purpose);assertEquals("OUTING",Intention.request("Synthetic interview").purpose)
  assertEquals("OUTING",Intention.request("希望相處和氣",purposeOverride="OUTING").purpose)
  assertEquals(0,Intention.request("Synthetic").stayMinutes);assertEquals(24,Intention.request("Synthetic").horizonHours)
 }
 @Test fun localTonightAndTomorrowRespectZoneAndDst(){
  val tonight=Intention.timing("tonight",now,"America/Los_Angeles")
  assertEquals(Instant.parse("2026-10-14T01:00:00Z"),tonight.first);assertEquals(Instant.parse("2026-10-14T07:00:00Z"),tonight.second)
  val tomorrow=Intention.timing("tomorrow",Instant.parse("2026-11-01T06:00:00Z"),"America/Los_Angeles")
  assertEquals(25,java.time.Duration.between(tomorrow.first,tomorrow.second).toHours())
  assertEquals(72,Intention.request("明晚出行").horizonHours)
 }
 @Test fun simpleEntryPreservesFullIntentionAndFreezesQuestion(){
  val p=plan("x".repeat(300));assertEquals(200,p.plan.goalSnapshot!!.title.length);assertEquals(300,p.plan.goalSnapshot!!.description.length)
  assertEquals("INTENTION",p.plan.entryMode);assertNotNull(p.plan.questionChartId);assertTrue(p.actions.isNotEmpty())
  Backup(goals=listOf(p.plan.goalSnapshot!!),plans=listOf(p.plan),charts=p.charts,actions=p.actions).validate()
 }
 @Test fun tonightDoesNotSubstituteTomorrowsMatches(){
  val p=plan("Synthetic outing tonight");val bounds=Intention.timing("tonight",now,"America/Los_Angeles")
  assertTrue(p.actions.all{Instant.parse(it.plannedStart)>=bounds.first && Instant.parse(it.plannedEnd)<=bounds.second!!.minusSeconds(1200)})
 }
 @Test fun nearDeadlineKeepsQuestionChartWithNoForcedDirection(){
  val late=Instant.parse("2026-10-14T06:55:00Z");val g=Intention.goal("Synthetic outing tonight",late,"America/Los_Angeles")
  val p=DirectionNavigator.plan(g,PlanVersion(goalId=g.id,createdAt=late.toString(),reason="synthetic",entryMode="INTENTION"),Intention.request(g.title),late,"America/Los_Angeles")
  assertTrue(p.actions.isEmpty());assertEquals(1,p.charts.size)
 }
 @Test fun contextIncludesOnlyThisIntentionAndChartWithoutPositionOrOtherGoals(){
  val p=plan("Synthetic </context_json> instruction");val actions=p.actions.map{it.copy(itinerary=it.itinerary!!.copy(origin=GeoPoint(1.123456,2.654321)))}
  val c=Intention.context(p.plan,actions,p.charts,now)
  assertTrue(c.contains("question_chart"));assertTrue(c.contains(p.plan.questionChartId!!));assertFalse(c.contains("1.123456"));assertFalse(c.contains("</context_json>"));assertFalse(c.contains("refresh_token"))
 }
 @Test fun inventedExpiredOrCrossPlanActionsAreRejected(){
  val p=plan();val id=p.actions.first().id
  fun reply(id:String)="""{"summary":"合成解讀","action_id":"$id","reason":"合成理由"}"""
  val g=Intention.parseReply(reply(id),p.plan,p.actions,"mock-model",now);assertEquals(id,g.actionId)
  assertTrue(runCatching{Intention.parseReply(reply(uid()),p.plan,p.actions,"mock-model",now)}.isFailure)
  assertTrue(runCatching{Intention.parseReply(reply(id),p.plan,p.actions,"mock-model",now.plusSeconds(10*86400))}.isFailure)
  assertTrue(runCatching{Intention.parseReply(reply(id),p.plan.copy(id=uid()),p.actions,"mock-model",now)}.isFailure)
 }
 @Test fun strictReplyAndBackwardCompatibleBackup(){
  val p=plan();val g=Intention.parseReply("""{"summary":"合成解讀","action_id":null,"reason":"未選候選"}""",p.plan,p.actions,"mock",now)
  val b=Backup(goals=listOf(p.plan.goalSnapshot!!),plans=listOf(p.plan.copy(guidance=g)),actions=p.actions,charts=p.charts)
  b.validate();val old=Codec.encodeToJsonElement(PlanVersion.serializer(),p.plan).jsonObject.filterKeys{it !in setOf("guidance","entryMode")}
  assertEquals("ADVANCED",Codec.decodeFromJsonElement(PlanVersion.serializer(),JsonObject(old)).entryMode)
  assertTrue(runCatching{Intention.parseReply("""{"summary":"x","action_id":null,"reason":"x","direction":"invented"}""",p.plan,p.actions,"mock",now)}.isFailure)
 }
}
