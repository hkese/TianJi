package com.tianji.journal
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.tianji.domain.*
import com.tianji.ui.*
@Composable fun JournalScreen(data:Backup,zone:String,action:ActionCandidate?,onSave:(JournalEntry)->Unit,onCancel:()->Unit){
 var outcome by remember(action?.id){mutableStateOf("UNKNOWN")};var notes by remember(action?.id){mutableStateOf("")};var score by remember(action?.id){mutableStateOf("")}
 Page(t("行動日誌","Action journal"),t("記錄實際發生的事，未知不當作失敗。","Record what happened. Unknown outcomes are not failures.")){
  if(action!=null)CardBlock(actionTitle(action.kind,action.title)){
   listOf("SUCCESS" to "完成","PARTIAL" to "部分完成","NOT_YET" to "未完成","UNKNOWN" to "未知").forEach{(value,zh)->FilterChip(outcome==value,{outcome=value},label={Text(outcomeLabel(value))})}
   Field(score,t("評分 0–10（選填）","Score 0–10 (optional)"),{score=it});Field(notes,t("觀察筆記","Observation notes"),{notes=it},single=false)
   ActionButton(t("儲存日誌","Save entry"),enabled=(score.isBlank() || score.toIntOrNull() in 0..10) && notes.length<=10000){onSave(JournalEntry(actionId=action.id,outcome=outcome,score=score.toIntOrNull(),notes=notes))}
   TextButton(onClick=onCancel){Text(t("取消","Cancel"))}
  }
  if(data.journal.isEmpty())Text(t("選擇一個方案後，記錄你的結果。","Choose a plan option, then record the result."))
  data.journal.sortedByDescending{it.occurredAt}.forEach{j->val a=data.actions.firstOrNull{it.id==j.actionId};CardBlock(a?.let{actionTitle(it.kind,it.title)}?:t("行動","Action")){
   Text(displayTime(j.occurredAt,zone)+" · "+outcomeLabel(j.outcome));j.score?.let{Text("$it / 10")};if(j.notes.isNotBlank())Text(j.notes);Text(t("計劃版本 ","Plan version ")+(data.plans.firstOrNull{it.id==a?.planVersionId}?.version?:"—")+t(" · 排盤未驗證"," · Chart unverified"),style=MaterialTheme.typography.bodySmall);Evidence()
  }}
 }
}
