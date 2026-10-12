package com.tianji.data
import android.content.Context
import androidx.room.*
import com.tianji.domain.*
import com.tianji.qimen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import java.time.Instant
class Repository(val db:TianJiDatabase) {
 private val dao=db.dao(); private val core=QimenCore()
 val state=db.invalidationTracker.createFlow("goals","milestones","charts","plans","actions","journal").map { db.withTransaction { snapshot() } }.flowOn(Dispatchers.IO)
 suspend fun snapshot()=Backup(
 goals=dao.goals().map{Codec.decodeFromString<Goal>(it.payload)},
 milestones=dao.milestones().map{Codec.decodeFromString<Milestone>(it.payload)},
 charts=dao.charts().map{Codec.decodeFromString<ChartSnapshot>(it.payload)},
 plans=dao.plans().map{Codec.decodeFromString<PlanVersion>(it.payload)},
 actions=dao.actions().map{Codec.decodeFromString<ActionCandidate>(it.payload)},
 journal=dao.journal().map{Codec.decodeFromString<JournalEntry>(it.payload)})
private suspend fun put(v:Goal)=dao.put(GRow(v.id,Codec.encodeToString(v)))
private suspend fun put(v:Milestone)=dao.put(MRow(v.id,v.goalId,Codec.encodeToString(v)))
private suspend fun put(v:ChartSnapshot)=dao.put(CRow(v.id,Codec.encodeToString(v)))
private suspend fun put(v:PlanVersion)=dao.put(PRow(v.id,v.goalId,Codec.encodeToString(v)))
private suspend fun put(v:ActionCandidate)=dao.put(ARow(v.id,v.planVersionId,v.chartId,Codec.encodeToString(v)))
private suspend fun put(v:JournalEntry)=dao.put(JRow(v.id,v.actionId,Codec.encodeToString(v)))
suspend fun saveChart(c:Chart) {
 val snapshot=ChartSnapshot(c.chart_id,c.input.instant_utc,c.input.timezone,c.ruleset.id,c.ruleset.version,core.canonical(c),c.input_hash)
 verifySnapshot(snapshot)
 put(snapshot)
}
suspend fun saveGoal(g:Goal) { validateGoal(g); db.withTransaction {
 // UPDATE existing rows rather than REPLACE: preserve children and history.
 val existing=dao.goals().firstOrNull{it.id==g.id}
 if(existing==null)put(g) else db.openHelper.writableDatabase.execSQL("UPDATE goals SET payload=? WHERE id=?",arrayOf(Codec.encodeToString(g),g.id))
} }
suspend fun saveMilestone(m:Milestone) { require(m.title.isNotBlank()); m.dueAt?.let(Instant::parse); put(m) }
suspend fun deleteGoal(id:String)=dao.deleteGoal(id)
suspend fun newPlan(goalId:String,zone:String,reason:String):PlanVersion=db.withTransaction {
 val b=snapshot(); val goal=b.goals.single{it.id==goalId}; val now=Instant.now(); val c=core.calculateChart(now,zone)
 val chart=ChartSnapshot(c.chart_id,c.input.instant_utc,c.input.timezone,c.ruleset.id,c.ruleset.version,core.canonical(c),c.input_hash)
 val plan=PlanVersion(goalId=goalId,parentVersionId=b.plans.filter{it.goalId==goalId}.maxByOrNull{it.createdAt}?.id,reason=reason,version=(b.plans.filter{it.goalId==goalId}.maxOfOrNull{it.version}?:0)+1)
 val actions=Planner.candidates(goal,plan,chart,now)
 put(chart);put(plan);actions.forEach{put(it)};plan
}
suspend fun choose(action:ActionCandidate)=db.withTransaction {
 snapshot().actions.filter{it.planVersionId==action.planVersionId}.forEach { a ->
  val updated=a.copy(selected=a.id==action.id)
  db.openHelper.writableDatabase.execSQL("UPDATE actions SET payload=? WHERE id=?",arrayOf(Codec.encodeToString(updated),a.id))
 }
}
suspend fun journal(entry:JournalEntry) { val b=snapshot(); b.copy(journal=b.journal+entry).validate();put(entry) }
suspend fun export():String=db.withTransaction { Codec.encodeToString(snapshot()) }
suspend fun restore(text:String) {
 require(text.toByteArray().size<=10*1024*1024) { "Backup exceeds 10 MiB" }
 val b=Codec.decodeFromString<Backup>(text);b.validate()
 b.charts.forEach { verifySnapshot(it) }
 db.withTransaction { clearRecords(); b.goals.forEach{put(it)};b.milestones.forEach{put(it)};b.charts.forEach{put(it)};b.plans.forEach{put(it)};b.actions.forEach{put(it)};b.journal.forEach{put(it)} }
}
private suspend fun clearRecords(){dao.clearjournal();dao.clearactions();dao.clearplans();dao.clearmilestones();dao.cleargoals();dao.clearcharts()}
suspend fun deleteAll(){db.withTransaction{clearRecords();dao.clearConsents();dao.clearProviders()}}
suspend fun provider()=dao.provider()?.let{AiProviderConfig(baseUrl=it.baseUrl,modelName=it.modelName,credentialAlias=it.credentialAlias)} ?: AiProviderConfig()
suspend fun provider(p:AiProviderConfig){ require(p.baseUrl.isEmpty() || validEndpoint(p.baseUrl));dao.provider(ProviderRow(p.id,p.baseUrl,p.modelName,p.credentialAlias)) }
suspend fun consent(scope:String)=dao.consent(ConsentRow(uid(),scope,Instant.now().toString(),null))
companion object { fun open(context:Context)=Repository(Room.databaseBuilder(context,TianJiDatabase::class.java,"tianji.db").addMigrations(MIGRATION_1_2).build()) }
}
