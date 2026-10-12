package com.tianji.data
import com.tianji.domain.*
import org.junit.Assert.*
import org.junit.Test
class AiTest {
 private val actions=(1..3).map{ActionCandidate(planVersionId=uid(),chartId=uid(),kind="CLARIFY",title="step",plannedStart="2026-01-01T00:00:00Z",plannedEnd="2026-01-01T00:10:00Z",rationale="test")}
 private fun json(claims:String="[]",extra:String="")="{\"action_order\":["+actions.joinToString(","){"\"${it.id}\""}+"],\"emphasis\":\"time\",\"chart_claims\":"+claims+extra+"}"
 @Test fun validComparisonAccepted(){assertEquals(3,AiClient().validate(json(),actions).action_order.size)}
 @Test(expected=IllegalArgumentException::class) fun chartClaimsRejected(){AiClient().validate(json("[\"fake door\"]"),actions)}
 @Test(expected=Exception::class) fun extraProbabilityRejected(){AiClient().validate(json(extra=",\"success_probability\":0.95"),actions)}
 @Test(expected=IllegalArgumentException::class) fun missingActionRejected(){AiClient().validate(json().replace(actions[0].id,actions[1].id),actions)}
 @Test fun endpointsRejectCredentialsAndHttp(){assertFalse(validEndpoint("http://example.com"));assertFalse(validEndpoint("https://secret@example.com"));assertFalse(validEndpoint("https://example.com?key=secret"));assertTrue(validEndpoint("https://example.com/v1"))}
 @Test fun quietHours(){assertTrue(quiet(22));assertTrue(quiet(7));assertFalse(quiet(8));assertFalse(quiet(21))}
}
