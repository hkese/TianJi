package com.tianji.goals
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tianji.domain.*
import com.tianji.ui.*
@Composable fun GoalsScreen(data:Backup,zone:String,onSave:(Goal)->Unit,onDelete:(String)->Unit,onMilestone:(Milestone)->Unit,onPlan:(String)->Unit){
 var edit by remember{mutableStateOf<Goal?>(null)};var creating by remember{mutableStateOf(false)};var deleting by remember{mutableStateOf<Goal?>(null)}
 if(creating || edit!=null){GoalEditor(edit,zone,onSave={onSave(it);creating=false;edit=null},onCancel={creating=false;edit=null});return}
 Page(t("目標","Goals"),t("將願望化成可以回顧的下一步。","Turn an intention into a step you can review.")){
  ActionButton(t("新增目標","New goal")){creating=true}
  if(data.goals.isEmpty())Text(t("未有目標。先寫下你想完成甚麼。","No goals yet. Write down what you want to accomplish."))
  data.goals.sortedByDescending{it.priority}.forEach{g->CardBlock(g.title){
   Text(g.successCriteria);Text(displayTime(g.deadline,zone)+" · "+goalStatus(g.status))
   data.milestones.filter{it.goalId==g.id}.forEach{m->Row{Checkbox(m.status=="DONE",{onMilestone(m.copy(status=if(it)"DONE" else "OPEN"))});Column{Text(m.title);Text(displayTime(m.dueAt,zone),style=MaterialTheme.typography.bodySmall)}}}
   var milestone by remember(g.id){mutableStateOf("")};var due by remember(g.id){mutableStateOf<String?>(null)}
   Field(milestone,t("新增里程碑","New milestone"),{milestone=it})
   if(milestone.isNotBlank()){DateField(due,zone,{due=it});TextButton(onClick={onMilestone(Milestone(goalId=g.id,title=milestone,dueAt=due));milestone="";due=null}){Text(t("加入里程碑","Add milestone"))}}
   ActionButton(t("規劃下一步","Plan next step")){onPlan(g.id)}
   Row{TextButton(onClick={edit=g}){Text(t("編輯","Edit"))};TextButton(onClick={deleting=g}){Text(t("刪除","Delete"))}}
  }}
 }
 deleting?.let{g->AlertDialog(onDismissRequest={deleting=null},title={Text(t("刪除目標？","Delete this goal?"))},text={Text(t("相關計劃與日誌亦會刪除。","Related plans and journal entries will also be deleted."))},confirmButton={TextButton(onClick={onDelete(g.id);deleting=null}){Text(t("刪除","Delete"))}},dismissButton={TextButton(onClick={deleting=null}){Text(t("取消","Cancel"))}})}
}
@OptIn(ExperimentalLayoutApi::class)
@Composable private fun GoalEditor(original:Goal?,zone:String,onSave:(Goal)->Unit,onCancel:()->Unit){
 var purpose by remember{mutableStateOf(original?.purpose?:"GENERAL")}
 var title by remember{mutableStateOf(original?.title?:"")};var criteria by remember{mutableStateOf(original?.successCriteria?:"")};var description by remember{mutableStateOf(original?.description?:"")};var minutes by remember{mutableStateOf((original?.constraints?.minutes?:20).toString())};var cost by remember{mutableStateOf((original?.constraints?.maxCost?:0.0).toString())};var notes by remember{mutableStateOf(original?.constraints?.notes?:"")};var deadline by remember{mutableStateOf(original?.deadline)};var priority by remember{mutableIntStateOf(original?.priority?:2)};var status by remember{mutableStateOf(original?.status?:"ACTIVE")};var error by remember{mutableStateOf("")}
 Page(t("設定目標","Edit goal")){
  Text(t("先填名稱同完成標準，其他可以用預設值。例如：讀一頁書，寫低一句重點。","Start with a title and success criteria; keep the other defaults. Example: read one page and write one takeaway."),style=MaterialTheme.typography.bodyMedium)
  Text(t("今次主要想做甚麼？", "What is this goal mainly about?"))
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){
   listOf("GENERAL" to t("其他／日常","Everyday"),"WORK" to t("工作／求職","Work"),"STUDY" to t("學習／練習","Learning"),"TALK" to t("溝通／關係","Communication")).forEach{(id,label)->FilterChip(purpose==id,{purpose=id},label={Text(label)})}
  }
  Field(title,t("目標名稱","Goal title"),{title=it});Field(criteria,t("怎樣算完成？","Success criteria"),{criteria=it});Field(description,t("描述","Description"),{description=it},single=false);DateField(deadline,zone,{deadline=it})
  Field(minutes,t("每次分鐘（5–240）","Minutes per action (5–240)"),{minutes=it});Field(cost,t("最高支出","Maximum cost"),{cost=it});Field(notes,t("其他限制（請自行核對）","Other constraints (review yourself)"),{notes=it},single=false)
  Text(t("優先次序","Priority"));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){(1..3).forEach{n->FilterChip(priority==n,{priority=n},label={Text(when(n){1->t("一般","Normal");2->t("重要","Important");else->t("最優先","Highest")})})}}
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("ACTIVE","PAUSED","DONE").forEach{s->FilterChip(status==s,{status=s},label={Text(goalStatus(s))})}}
  ErrorText(error)
  val requiredError=t("請填寫目標名稱及完成標準。","Enter a goal title and success criteria.")
  val inputError=t("請檢查分鐘（5–240）、支出及限期。","Check duration (5–240 minutes), cost and deadline.")
  ActionButton(t("儲存目標","Save goal")){runCatching{val g=(original?:Goal(title=title,successCriteria=criteria)).copy(title=title,purpose=purpose,description=description,successCriteria=criteria,deadline=deadline,priority=priority,status=status,constraints=Constraints(minutes.toInt(),cost.toDouble(),notes));validateGoal(g);g}.onSuccess(onSave).onFailure{error=if(title.isBlank()||criteria.isBlank())requiredError else inputError}}
  OutlinedButton(onClick=onCancel,modifier=Modifier.fillMaxWidth()){Text(t("取消","Cancel"))}
 }
}
