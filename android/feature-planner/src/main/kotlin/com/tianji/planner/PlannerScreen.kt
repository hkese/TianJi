package com.tianji.planner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.tianji.domain.*
import com.tianji.ui.*
@Composable fun PlannerScreen(goalId:String,data:Backup,zone:String,onNew:(String)->Unit,onChoose:(ActionCandidate)->Unit,onJournal:(ActionCandidate)->Unit,onAi:(Goal,List<ActionCandidate>)->Unit,onBack:()->Unit){
 val goal=data.goals.firstOrNull{it.id==goalId}?:return;val plans=data.plans.filter{it.goalId==goalId}.sortedByDescending{it.createdAt};var shown by remember(goalId){mutableStateOf<String?>(null)};val plan=plans.firstOrNull{it.id==shown}?:plans.firstOrNull();var reason by remember{mutableStateOf("")}
 val initialReason=t("首次計劃","Initial plan")
 Page(t("下一步","Next step"),goal.title){
  TextButton(onClick=onBack){Text(t("返回目標","Back to goals"))}
  if(plan==null)ActionButton(t("產生三個方案","Generate three options")){onNew(initialReason)}
  else{
   Text(t("計劃版本：","Plan version: ")+plan.version.toString()+" · "+displayTime(plan.createdAt,zone));Text(plan.reason)
   val actions=data.actions.filter{it.planVersionId==plan.id}
   actions.forEach{a->CardBlock(if(English.current)when(a.kind){"CLARIFY"->"Clarify the completion criteria";"SMALL_STEP"->"Take the smallest useful step";else->"Review friction and the next step"} else a.title){
    Text(displayTime(a.plannedStart,zone)+" → "+displayTime(a.plannedEnd,zone));Text(t("支出：0 · 風險：低 · 地點自選","Cost: 0 · Risk: low · Location: your choice"));Text(if(English.current)"A bounded, reversible planning step based on your time and cost limits. Review free-text constraints before acting." else a.rationale);Evidence()
    ActionButton(if(a.selected)t("已選擇","Selected") else t("選擇此方案","Choose this option")){onChoose(a)}
    if(a.selected)OutlinedButton(onClick={onJournal(a)}){Text(t("記錄結果","Record outcome"))}
   }}
   if(actions.isNotEmpty())OutlinedButton(onClick={onAi(goal,actions)}){Text(t("AI 比較（先檢視分享內容）","AI comparison (review context first)"))}
  }
  Field(reason,t("新觀察／改計劃原因","New observation / reason to replan"),{reason=it},single=false)
  ActionButton(t("重新規劃，保留歷史","Replan and preserve history"),enabled=reason.isNotBlank()){onNew(reason);shown=null;reason=""}
  if(plans.isNotEmpty())CardBlock(t("歷史版本","Plan history")){plans.forEach{p->TextButton(onClick={shown=p.id}){Text(displayTime(p.createdAt,zone)+" · "+p.reason)}}}
 }
}
