package com.tianji.planner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tianji.ui.*

@Composable internal fun AnalysisHandoff(prompt:String,onClose:()->Unit){
 val context=LocalContext.current
 var copied by remember{mutableStateOf(false)}
 var error by remember{mutableStateOf(false)}
 var preview by remember{mutableStateOf(false)}
 Dialog(onDismissRequest=onClose,properties=DialogProperties(usePlatformDefaultWidth=false)){
  Surface(Modifier.fillMaxSize().safeDrawingPadding()){
   Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
    Text(t("帶住呢個盤問 AI","Ask AI with this chart"),style=MaterialTheme.typography.headlineSmall)
    Text(t("1 · 檢視下面內容，確認冇唔想分享嘅資料。\n2 · 按「複製完整提示詞」。\n3 · 開 ChatGPT 或你使用的 Codex 對話，貼上再送出。\n4 · 睇完返嚟揀方案，做完記錄結果。","1 · Review the content below.\n2 · Copy the complete prompt.\n3 · Open a ChatGPT or Codex conversation, paste and send.\n4 · Return to choose an option and record the result."))
    Text(t("分享內容以預覽為準：目標、限制、候選方案及相應完整盤；新行程包含擇時擇方規則。唔包括日誌筆記、其他目標、API 金鑰或 GPS。只有你喺對方服務按送出，先會交畀佢分析。","Review the preview: it contains your goal, constraints, candidate options and their charts; new itineraries include time and direction rules. Excludes journal notes, other goals, API keys and GPS. You send it yourself in the other service."),style=MaterialTheme.typography.bodySmall)
    OutlinedButton(onClick={preview=!preview},modifier=Modifier.fillMaxWidth().testTag("handoff-preview")){Text(if(preview)t("收起完整內容","Hide complete content") else t("檢視完整分享內容","Review complete content"))}
    if(preview)SelectionContainer{Text(prompt,style=MaterialTheme.typography.bodySmall,modifier=Modifier.testTag("handoff-prompt"))}
    ActionButton(if(copied)t("已複製 · 下一步開 ChatGPT","Copied · open ChatGPT next") else t("複製完整提示詞","Copy complete prompt")){
     val clipboard=context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
     clipboard.setPrimaryClip(ClipData.newPlainText("TianJi 分析提示詞",prompt));copied=true
    }
    OutlinedButton(onClick={
     error=runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://chatgpt.com/")))}.isFailure
    },modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(t("開啟 ChatGPT（貼上後送出）","Open ChatGPT (paste, then send)"))}
    OutlinedButton(onClick={
     error=runCatching{context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,prompt)},"TianJi"))}.isFailure
    },modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(t("用手機分享選單傳送","Open phone share menu"))}
    if(error)ErrorText(t("未能開啟其他 App。請先複製，再自行開啟對話貼上。","Could not open another app. Copy the prompt and paste it into a conversation yourself."))
    Text(t("呢個係手動交接，AI 回覆唔會自動寫入天機。你可以將重點寫入「新觀察」，再重新規劃。未經核實的 AI 解讀仍只是參考。","This is a manual handoff; the answer is not automatically imported. Add your takeaway as a new observation and replan. AI interpretations still need review."),style=MaterialTheme.typography.bodySmall)
    TextButton(onClick=onClose,modifier=Modifier.fillMaxWidth()){Text(t("返回天機方案","Back to TianJi options"))}
   }
  }
 }
}
