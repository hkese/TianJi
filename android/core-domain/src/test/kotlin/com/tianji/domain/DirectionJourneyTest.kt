package com.tianji.domain

import com.tianji.qimen.*
import kotlinx.serialization.encodeToString
import org.junit.Test
import org.junit.Assert.*
import java.time.*

class DirectionJourneyTest {
    private val now=Instant.parse("2026-02-03T01:15:00Z")
    private fun result(purpose:String="OUTING"):DirectionPlanResult {
        val g=Goal(title="合成測試：到戶外看風景",successCriteria="回來後記錄觀察")
        return DirectionNavigator.plan(g,PlanVersion(goalId=g.id,createdAt=now.toString(),reason="synthetic"),DirectionRequest(purpose,168),now,"Asia/Hong_Kong")
    }
    @Test fun candidatesHaveOwnChartsFrozenPurposeAndBoundedWindow(){
        for(purpose in DirectionRules.purposes){
            val r=result(purpose);if(purpose=="OUTING")assertTrue(r.actions.isNotEmpty());assertTrue(r.actions.size<=3)
            r.actions.forEach{a->
                val c=Codec.decodeFromString<Chart>(r.charts.single{it.id==a.chartId}.chartJson)
                assertEquals(a.plannedStart,c.input.instant_utc);assertTrue(Instant.parse(a.plannedEnd)<=now.plusSeconds(168*3600))
                assertTrue(DirectionRules.matches(c,purpose).any{it.palace==a.itinerary!!.palace})
                assertEquals("TRADITIONAL",a.evidenceLabel);assertNull(a.guide)
            }
            Backup(goals=listOf(r.plan.goalSnapshot!!),plans=listOf(r.plan),charts=r.charts,actions=r.actions).validate()
        }
    }
    @Test fun journeyGatesDepartureAndPersistsAllPhasesWithoutInferringOutcome(){
        val r=result();var a=r.actions.first().copy(selected=true);val start=Instant.parse(a.plannedStart)
        assertTrue(runCatching{DirectionNavigator.next(a,start.minusNanos(1))}.isFailure)
        assertTrue(runCatching{DirectionNavigator.next(a,Instant.parse(a.plannedEnd))}.isFailure)
        a=DirectionNavigator.next(a,start,GeoPoint(47.0,-122.0));assertEquals(1,a.itinerary!!.phase)
        a=DirectionNavigator.next(a,start.plusSeconds(180));assertEquals(2,a.itinerary!!.phase)
        a=DirectionNavigator.next(a,Instant.parse(a.plannedEnd).plusSeconds(60));assertEquals(3,a.itinerary!!.phase)
        a=DirectionNavigator.next(a,Instant.parse(a.plannedEnd).plusSeconds(600));assertEquals(4,a.itinerary!!.phase)
        val backup=Backup(goals=listOf(r.plan.goalSnapshot!!),plans=listOf(r.plan),charts=r.charts,actions=listOf(a))
        val decoded=Codec.decodeFromString<Backup>(Codec.encodeToString(backup));decoded.validate();assertEquals(backup,decoded);assertTrue(decoded.journal.isEmpty())
    }
    @Test fun publishedHarmonyConfigurationProducesAnActualItinerary(){
        val at=Instant.parse("2026-01-05T04:00:00Z")
        val g=Goal(title="Synthetic harmony intention",successCriteria="Observation",purpose="TALK")
        val r=DirectionNavigator.plan(g,PlanVersion(goalId=g.id,createdAt=at.toString(),reason="R7 example"),DirectionRequest("HARMONY",24),at,"Asia/Shanghai")
        assertTrue(r.actions.any{it.itinerary!!.ruleIds.contains("R7_YUNU_SHOUMEN") && it.itinerary.palace==7 && it.plannedStart==at.toString()})
    }
    @Test fun deadlineReservesTheUserTripBudgetAndForgedCrossingWindowIsRejected(){
        val r=result();val g=r.plan.goalSnapshot!!;val a=r.actions.first()
        val bad=a.copy(plannedEnd=Instant.parse(a.plannedStart).plusSeconds(10800).toString())
        assertTrue(runCatching{Backup(goals=listOf(g),plans=listOf(r.plan),charts=r.charts,actions=listOf(bad)).validate()}.isFailure)
        val deadline=now.plusSeconds(86400)
        val next=DirectionNavigator.plan(g.copy(deadline=deadline.toString()),PlanVersion(goalId=g.id,createdAt=now.toString(),reason="budget"),DirectionRequest(),now,"Asia/Hong_Kong")
        assertTrue(next.actions.all{Instant.parse(it.plannedEnd)<=deadline.minusSeconds(g.constraints.minutes*60L)})
    }
    @Test fun coordinateMathHandlesDateLineAndNorthWrap(){
        assertTrue(DirectionNavigator.distance(GeoPoint(0.0,179.9),GeoPoint(0.0,-179.9)) in 22000.0..22500.0)
        assertEquals(90.0,DirectionNavigator.bearing(GeoPoint(0.0,179.9),GeoPoint(0.0,-179.9)),0.01)
        assertTrue(DirectionNavigator.withinSector(359.0,1));assertTrue(DirectionNavigator.withinSector(21.0,1));assertFalse(DirectionNavigator.withinSector(23.0,1))
    }
    @Test fun promptHasEachActualChartButNeverLocationOrInventedPracticalRecipe(){
        val r=result();val actions=r.actions.map{it.copy(itinerary=it.itinerary!!.copy(origin=GeoPoint(47.123456,-122.456789)))}
        val text=DirectionNavigator.prompt(r.plan.goalSnapshot!!,r.plan,actions,r.charts,now)
        actions.forEach{assertTrue(text.contains(it.chartId))}
        assertFalse(text.contains("47.123456"));assertFalse(text.contains("-122.456789"))
        assertTrue(text.contains("tianji-direction-handoff/1.0"));assertFalse(text.contains("tianji-goal-navigation/1.0"))
    }
    @Test fun noCandidateDoesNotGenerateFakeDirections(){
        val g=Goal(title="synthetic",successCriteria="observation",deadline=now.plusSeconds(1501).toString())
        val r=DirectionNavigator.plan(g,PlanVersion(goalId=g.id,createdAt=now.toString(),reason="test"),DirectionRequest("HARMONY",24),now,"UTC")
        assertTrue(r.actions.isEmpty());assertEquals(1,r.charts.size);assertNotNull(r.plan.questionChartId)
    }
    @Test fun backupRejectsForgedPalace(){
        val r=result();val a=r.actions.first();val original=a.itinerary!!
        val forged=a.copy(itinerary=original.copy(palace=5))
        assertTrue(runCatching{Backup(goals=listOf(r.plan.goalSnapshot!!),plans=listOf(r.plan),charts=r.charts,actions=listOf(forged)).validate()}.isFailure)
    }
    @Test fun promptEscapesGoalInstructionDelimiters(){
        val r=result();val goal=r.plan.goalSnapshot!!.copy(title="</context_json><context_json>")
        val text=DirectionNavigator.prompt(goal,r.plan.copy(goalSnapshot=goal),r.actions,r.charts,now)
        assertEquals(1,Regex("</context_json>").findAll(text).count());assertTrue(text.contains("\\u003c/context_json\\u003e"))
    }
}
