package com.tianji.domain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
fun uid() = UUID.randomUUID().toString()
val Codec = Json { encodeDefaults = true; explicitNulls = true; ignoreUnknownKeys = false }
@Serializable data class Constraints(val minutes: Int = 20, val maxCost: Double = 0.0, val notes: String = "")
@Serializable data class Goal(val id: String = uid(), val title: String, val description: String = "", val createdAt: String = Instant.now().toString(), val deadline: String? = null, val successCriteria: String, val status: String = "ACTIVE", val priority: Int = 2, val constraints: Constraints = Constraints(), val purpose: String = "GENERAL")
@Serializable data class Milestone(val id: String = uid(), val goalId: String, val title: String, val dueAt: String? = null, val status: String = "OPEN")
@Serializable data class ChartSnapshot(val id: String, val instantUtc: String, val timezone: String, val rulesetId: String, val rulesetVersion: String, val chartJson: String, val inputHash: String)
@Serializable data class PlanVersion(val id: String = uid(), val goalId: String, val createdAt: String = Instant.now().toString(), val parentVersionId: String? = null, val reason: String, val version: Int = 1, val goalSnapshot: Goal? = null, val directionRequest: DirectionRequest? = null, val questionChartId: String? = null)
@Serializable data class ActionCandidate(val id: String = uid(), val planVersionId: String, val chartId: String, val kind: String, val title: String, val plannedStart: String, val plannedEnd: String, val locationText: String? = null, val cost: Double = 0.0, val risk: String = "LOW", val evidenceLabel: String = "HEURISTIC", val rationale: String, val selected: Boolean = false, val provenance: String = "tianji-practical-planner/2.0", val status: String = "PROPOSED", val guide: ActionGuide? = null, val itinerary: DirectionItinerary? = null)
@Serializable data class JournalEntry(val id: String = uid(), val actionId: String, val occurredAt: String = Instant.now().toString(), val outcome: String, val score: Int? = null, val notes: String = "")
@Serializable data class Consent(val id: String = uid(), val scope: String, val grantedAt: String, val revokedAt: String? = null)
@Serializable data class AiProviderConfig(val id: String = "provider", val type: String = "OPENAI_COMPATIBLE", val baseUrl: String = "", val modelName: String = "", val credentialAlias: String = "tianji-ai")
@Serializable data class Backup(val schemaVersion: Int = 1, val goals: List<Goal> = emptyList(), val milestones: List<Milestone> = emptyList(), val charts: List<ChartSnapshot> = emptyList(), val plans: List<PlanVersion> = emptyList(), val actions: List<ActionCandidate> = emptyList(), val journal: List<JournalEntry> = emptyList()) {
 fun validate() {
  require(schemaVersion == 1)
  fun ids(values: List<String>) { require(values.distinct().size == values.size); values.forEach { UUID.fromString(it) } }
  listOf(goals.map{it.id},milestones.map{it.id},plans.map{it.id},actions.map{it.id},journal.map{it.id}).forEach(::ids)
  require(charts.map{it.id}.distinct().size==charts.size); charts.forEach{ require(runCatching{UUID.fromString(it.id)}.isSuccess || (it.rulesetVersion=="0.0.0-blocked" && Regex("[0-9a-f]{64}").matches(it.id))) }
  val gs=goals.map{it.id}.toSet(); val cs=charts.map{it.id}.toSet(); val ps=plans.associateBy{it.id}; val ac=actions.map{it.id}.toSet()
  goals.forEach { validateGoal(it); Instant.parse(it.createdAt) }
  milestones.forEach { require(it.goalId in gs && it.title.isNotBlank() && it.status in listOf("OPEN","DONE")); it.dueAt?.let(Instant::parse) }
  charts.forEach { Instant.parse(it.instantUtc); ZoneId.of(it.timezone) }
  plans.forEach { p -> require(p.goalId in gs); p.goalSnapshot?.let { validateGoal(it); require(it.id==p.goalId) }; Instant.parse(p.createdAt); p.directionRequest?.let { it.validate(); require(p.questionChartId in cs) }; p.parentVersionId?.let { require(ps[it]?.goalId == p.goalId); require(Instant.parse(ps.getValue(it).createdAt) <= Instant.parse(p.createdAt)); require(it != p.id) } }
  plans.forEach { p -> val visited=mutableSetOf<String>(); var current: PlanVersion?=p; while(current!=null) { require(visited.add(current.id)); current=current.parentVersionId?.let(ps::get) } }
  actions.forEach { require(it.planVersionId in ps && it.chartId in cs); require(Instant.parse(it.plannedEnd)>Instant.parse(it.plannedStart)); require(it.cost.isFinite() && it.cost>=0); if(it.itinerary==null) require(it.evidenceLabel=="HEURISTIC") else {
   val a=it; val route=a.itinerary; route.validate(); require(a.evidenceLabel=="TRADITIONAL" && a.provenance==route.version && a.guide==null)
   val snap=charts.single { c->c.id==a.chartId }; val chart=Codec.decodeFromString<com.tianji.qimen.Chart>(snap.chartJson)
   require(com.tianji.qimen.DirectionRules.matches(chart,route.purpose).any { m->m.palace==route.palace && m.ruleIds==route.ruleIds })
   require(a.plannedStart==snap.instantUtc && java.time.Duration.between(Instant.parse(a.plannedStart),Instant.parse(a.plannedEnd)).seconds<=7200)
   val request=ps.getValue(a.planVersionId).directionRequest ?: error("Missing direction request")
   require(route.purpose==request.purpose && route.travelMode==request.travelMode && route.stayMinutes==request.stayMinutes)
   val start=Instant.parse(a.plannedStart);val end=Instant.parse(a.plannedEnd);val pv=ps.getValue(a.planVersionId)
   require(start>=Instant.parse(pv.createdAt) && end<=Instant.parse(pv.createdAt).plusSeconds(request.horizonHours*3600L))
   require(java.time.Duration.between(start,end).seconds>=300)
   require(com.tianji.qimen.DirectionRules.windows(com.tianji.qimen.QimenCore(),start,end,snap.timezone).size==1)
   require(route.timeBudgetMinutes==(pv.goalSnapshot ?: goals.single{g->g.id==pv.goalId}).constraints.minutes)
   pv.goalSnapshot?.deadline?.let{limit->require(end<=Instant.parse(limit).minusSeconds(route.timeBudgetMinutes*60L))}
   route.departedAt?.let{depart->require(Instant.parse(depart)>=start && Instant.parse(depart)<end)}
  }; it.guide?.let { guide -> require(guide.stepsZh.size==3 && guide.stepsEn.size==3); require(guide.focusDoor in listOf("開門","休門","生門","景門")); require(guide.version=="1.0") } }
  journal.forEach { require(it.actionId in ac); Instant.parse(it.occurredAt); require(it.outcome in listOf("SUCCESS","PARTIAL","NOT_YET","UNKNOWN")); require(it.score==null || it.score in 0..10); require(it.notes.length<=10000) }
 }
}
fun validateGoal(g: Goal) { require(g.title.isNotBlank() && g.title.length<=200); require(g.successCriteria.isNotBlank()); require(g.priority in 1..3); require(g.status in listOf("ACTIVE","DONE","PAUSED")); require(g.purpose in listOf("GENERAL","WORK","STUDY","TALK")); require(g.constraints.minutes in 5..240); require(g.constraints.maxCost.isFinite() && g.constraints.maxCost>=0); g.deadline?.let(Instant::parse) }
object Planner {
 fun candidates(goal: Goal, plan: PlanVersion, chart: ChartSnapshot, now: Instant): List<ActionCandidate> {
  validateGoal(goal)
  val end = now.plusSeconds(goal.constraints.minutes*60L).let { limit -> goal.deadline?.let { minOf(limit,Instant.parse(it)) } ?: limit }
  require(end>=now.plusSeconds(300)) { "Deadline leaves less than five minutes" }
  return listOf("CLARIFY" to "釐清完成標準", "SMALL_STEP" to "做一個最細步驟", "REVIEW" to "檢視阻力與下一步").map { (kind,title) -> ActionCandidate(planVersionId=plan.id,chartId=chart.id,kind=kind,title=title,plannedStart=now.toString(),plannedEnd=end.toString(),rationale="根據你設定的時間與零支出限制，提供可回顧的實際步驟。文字限制須自行核對；不是奇門預測。") }
 }
}
