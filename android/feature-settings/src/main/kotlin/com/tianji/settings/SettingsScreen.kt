package com.tianji.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.tianji.domain.*
import com.tianji.data.*
import com.tianji.ui.*
import java.time.ZoneId
@Composable fun SettingsScreen(s:Settings,provider:AiProviderConfig,onSettings:(Settings)->Unit,onProvider:(AiProviderConfig,String)->Unit,onTest:()->Unit,onExport:()->Unit,onImport:()->Unit,onResearch:()->Unit,onDelete:()->Unit){
 var zone by remember(s.timezone){mutableStateOf(s.timezone)};var url by remember(provider.baseUrl){mutableStateOf(provider.baseUrl)};var model by remember(provider.modelName){mutableStateOf(provider.modelName)};var key by remember{mutableStateOf("")};var deleting by remember{mutableStateOf(false)}
 Page(t("設定","Settings")){
  CardBlock(t("顯示與時間","Display and time")){
   Row{Text("English",Modifier.weight(1f));Switch(s.english,{onSettings(s.copy(english=it))})}
   listOf("system","light","dark").forEach{v->FilterChip(s.theme==v,{onSettings(s.copy(theme=v))},label={Text(v)})}
   Field(zone,t("IANA 時區","IANA timezone"),{zone=it});ActionButton(t("套用時區","Apply timezone"),enabled=runCatching{ZoneId.of(zone)}.isSuccess){onSettings(s.copy(timezone=zone))}
  }
  CardBlock(t("提醒","Reminders")){Row{Text(t("每 12 小時溫和提醒","Gentle reminder every 12 hours"),Modifier.weight(1f));Switch(s.reminders,{onSettings(s.copy(reminders=it))})};Text(t("安靜時段 22:00–08:00；Android 可延後執行。","Quiet hours 22:00–08:00; Android may defer delivery."))}
  CardBlock(t("可選 AI 供應商","Optional AI provider")){
   Text(t("沒有 AI 亦可使用所有離線規劃功能。儲存設定不會傳送目標；每次比較都會先顯示分享內容。","Offline planning works without AI. Saving does not share goals; each comparison requires payload review."))
   Field(url,"HTTPS base URL",{url=it});Field(model,t("模型名稱","Model name"),{model=it});OutlinedTextField(key,{key=it},label={Text(t("API 金鑰（只儲存加密內容）","API key (encrypted storage)"))},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
   ActionButton(t("儲存供應商","Save provider"),enabled=url.isEmpty()||validEndpoint(url)){onProvider(provider.copy(baseUrl=url,modelName=model),key);key=""}
   OutlinedButton(onClick=onTest,enabled=provider.baseUrl.isNotBlank()){Text(t("測試連線（發送至供應商）","Test connection (contacts provider)"))}
  }
  CardBlock(t("你的資料","Your data")){ActionButton(t("匯出 JSON 備份","Export JSON backup"),onClick=onExport);OutlinedButton(onClick=onImport){Text(t("匯入備份（取代目前資料）","Import backup (replace current data)"))};OutlinedButton(onClick=onResearch){Text(t("檢視研究匯出同意","Review research export consent"))};TextButton(onClick={deleting=true}){Text(t("刪除所有本機資料","Delete all local data"))}}
  CardBlock(t("證據與限制","Evidence and limitations")){Text(t("TRADITIONAL：傳統解讀；HEURISTIC：實用規劃；RESEARCH_ONLY：探索結果；VALIDATED：需獨立實證。現時建議全部為 HEURISTIC，並無 VALIDATED 預測。","TRADITIONAL: traditional interpretation; HEURISTIC: practical planning; RESEARCH_ONLY: exploratory results; VALIDATED: requires independent empirical evidence. Current suggestions are HEURISTIC; there are no VALIDATED predictions."));Text(t("奇門符號與日曆評級待驗證（0/100 參考盤）。模型匯入及 Transformer 尚未啟用。","Qimen symbols and opportunity calendar await verification (0/100 fixtures). Model import and Transformer are not enabled."))}
 }
 if(deleting)AlertDialog(onDismissRequest={deleting=false},title={Text(t("刪除所有資料？","Delete all data?"))},text={Text(t("會刪除目標、計劃、日誌、同意及 API 金鑰。已匯出的檔案不受影響。","Deletes goals, plans, journal, consents and API key. Previously exported files remain under your control."))},confirmButton={TextButton(onClick={onDelete();deleting=false}){Text(t("確認刪除","Delete"))}},dismissButton={TextButton(onClick={deleting=false}){Text(t("取消","Cancel"))}})
}
