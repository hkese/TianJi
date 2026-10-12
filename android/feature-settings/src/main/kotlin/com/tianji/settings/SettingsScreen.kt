package com.tianji.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tianji.domain.*
import com.tianji.data.*
import com.tianji.ui.*

@OptIn(ExperimentalLayoutApi::class)
@Composable fun SettingsScreen(s:Settings,provider:AiProviderConfig,onSettings:(Settings)->Unit,onProvider:(AiProviderConfig,String)->Unit,onTest:()->Unit,onExport:()->Unit,onImport:()->Unit,onResearch:()->Unit,onDelete:()->Unit,onGuide:()->Unit){
 var url by remember(provider.baseUrl){mutableStateOf(provider.baseUrl)}
 var model by remember(provider.modelName){mutableStateOf(provider.modelName)}
 var key by remember{mutableStateOf("")}
 var deleting by remember{mutableStateOf(false)}
 var advanced by rememberSaveable{mutableStateOf(false)}
 Page(t("設定","Settings")){
  CardBlock(t("顯示與時間","Display and time")){
   LanguageSelector(s.english){onSettings(s.copy(english=it))}
   Text(t("外觀","Appearance"),style=MaterialTheme.typography.titleMedium)
   FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){
    listOf("system" to t("跟隨系統","System"),"light" to t("淺色","Light"),"dark" to t("深色","Dark")).forEach{(v,label)->
     FilterChip(s.theme==v,{onSettings(s.copy(theme=v))},modifier=Modifier.testTag("theme-$v"),label={Text(label)})
    }
   }
   TimeZoneSelector(s.timezone){onSettings(s.copy(timezone=it))}
   Text(t("選擇後即時儲存。已有記錄的實際時間不會改變。","Your choice is saved immediately. Existing records keep the same actual instant."),style=MaterialTheme.typography.bodySmall)
  }
  CardBlock(t("唔知點開始？","Need a hand?")){
   Text(t("跟住四個步驟，由建立目標到記錄結果。","Follow four steps from creating a goal to recording the outcome."))
   ActionButton(t("使用教學","Getting started"),onClick=onGuide)
  }
  CardBlock(t("提醒","Reminders")){
   Row{Text(t("每 12 小時溫和提醒","Gentle reminder every 12 hours"),Modifier.weight(1f));Switch(s.reminders,{onSettings(s.copy(reminders=it))})}
   Text(t("安靜時段 22:00–08:00，按所選時區計算；手機系統可能延後通知。","Quiet hours 22:00–08:00 in your selected zone; Android may defer delivery."))
  }
  CardBlock(t("你的資料","Your data")){
   ActionButton(t("匯出 JSON 備份","Export JSON backup"),onClick=onExport)
   OutlinedButton(onClick=onImport){Text(t("匯入備份（取代目前資料）","Import backup (replace current data)"))}
   TextButton(onClick={deleting=true}){Text(t("刪除所有本機資料","Delete all local data"))}
  }
  OutlinedButton(onClick={advanced=!advanced},modifier=Modifier.fillMaxWidth()){
   Text(if(advanced)t("收起進階設定","Hide advanced settings") else t("進階設定：可選 AI 與研究","Advanced: optional AI and research"))
  }
  if(advanced){
   CardBlock(t("可選 AI 供應商","Optional AI provider")){
    Text(t("沒有 AI 亦可使用所有離線規劃功能。儲存設定不會傳送目標；每次比較都會先顯示分享內容。AI 不能啟用奇門排盤。","Offline planning works without AI. Saving does not share goals; each comparison requires payload review. AI cannot enable Qimen calculation."))
    Field(url,t("供應商網址（HTTPS）","Provider URL (HTTPS)"),{url=it})
    Field(model,t("模型名稱","Model name"),{model=it})
    OutlinedTextField(key,{key=it},label={Text(t("API 金鑰（加密儲存）","API key (encrypted storage)"))},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
    ActionButton(t("儲存供應商","Save provider"),enabled=url.isEmpty()||validEndpoint(url)){onProvider(provider.copy(baseUrl=url,modelName=model),key);key=""}
    OutlinedButton(onClick=onTest,enabled=provider.baseUrl.isNotBlank()){Text(t("測試連線（發送至供應商）","Test connection (contacts provider)"))}
   }
   OutlinedButton(onClick=onResearch){Text(t("檢視研究匯出同意","Review research export consent"))}
  }
  CardBlock(t("證據與限制","Evidence and limitations")){
   Text(t("實用規劃：按目標及限制提供步驟，並非預測。傳統解讀：不代表科學驗證。研究結果：只供探索。實證估計：必須先有獨立驗證；目前沒有這類預測。","Heuristic planning offers steps based on goals and constraints, not predictions. Traditional interpretation is not scientific validation. Research results are exploratory. Validated estimates require independent evidence; none are currently available."))
   Text(t("基本拆補轉盤及傳統簡讀已可離線使用。100 個獨立人工金標仍未完成；吉時日曆、模型匯入及進階研究模型未啟用。","Basic Chai Bu charts and traditional readings work offline. The 100 human-golden target remains incomplete. Opportunity calendars, model import and advanced research models are not enabled."))
  }
 }
 if(deleting)AlertDialog(onDismissRequest={deleting=false},title={Text(t("刪除所有資料？","Delete all data?"))},text={Text(t("會刪除目標、計劃、日誌、同意及 API 金鑰。已匯出的檔案不受影響。","Deletes goals, plans, journal, consents and API key. Previously exported files remain under your control."))},confirmButton={TextButton(onClick={onDelete();deleting=false}){Text(t("確認刪除","Delete"))}},dismissButton={TextButton(onClick={deleting=false}){Text(t("取消","Cancel"))}})
}
