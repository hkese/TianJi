package com.tianji.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PersistableBundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import com.tianji.data.*
import com.tianji.domain.*
import com.tianji.home.HomeScreen
import com.tianji.qimen.*
import com.tianji.ui.*
import kotlinx.coroutines.delay
import java.time.*

@OptIn(ExperimentalLayoutApi::class,ExperimentalFoundationApi::class)
@Composable fun IntentionScreen(vm:AppModel,state:UiState,chat:ChatUiState,working:Boolean,message:String,busy:Boolean,onPlan:(String)->Unit,onChart:()->Unit,onGuide:()->Unit,onGoals:()->Unit,onSettings:()->Unit){
 var text by rememberSaveable{mutableStateOf("")};var advanced by rememberSaveable{mutableStateOf(false)}
 var history by rememberSaveable{mutableStateOf(false)};var account by rememberSaveable{mutableStateOf(false)}
 var hours by rememberSaveable{mutableIntStateOf(0)};var purpose by rememberSaveable{mutableStateOf<String?>(null)}
 var details by rememberSaveable{mutableStateOf(false)}
 var revealResult by remember{mutableStateOf(false)};val resultFocus=remember{BringIntoViewRequester()}
 var now by remember{mutableStateOf(Instant.now())};LaunchedEffect(Unit){while(true){now=Instant.now();delay(1000)}}
 val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
 val data=state.data;val zone=state.settings.timezone
 if(history){Column{TextButton(onClick={history=false}){Text(t("返回心願","Back to intention"))};Box(Modifier.weight(1f)){HomeScreen(data,zone,onGoals,onPlan,onChart,onGuide)}};return}
 if(account){ChatAccountScreen(vm,chat,{account=false},onSettings);return}
 val plan=data.plans.filter{it.entryMode=="INTENTION"}.maxByOrNull{it.createdAt}
 val actions=data.actions.filter{it.planVersionId==plan?.id}
 val selected=actions.firstOrNull{it.selected}
 val snapshot=data.charts.firstOrNull{it.id==plan?.questionChartId}
 val question=remember(snapshot?.id){snapshot?.let{Codec.decodeFromString<Chart>(it.chartJson)}}
 LaunchedEffect(plan?.id){if(plan!=null && revealResult){withFrameNanos{};resultFocus.bringIntoView();revealResult=false}}
 Page(t("你而家有咩心願？","What's your intention?"),t("寫低就得，天機幫你起盤同整理指引。","Write it down. TianJi prepares the chart and guidance.")){
  OutlinedTextField(value=text,onValueChange={if(it.length<=1000)text=it},label={Text(t("我想……","I'd like…"))},placeholder={Text(t("例如：希望今晚相處和氣","For example: a harmonious evening"))},minLines=2,maxLines=5,modifier=Modifier.fillMaxWidth().testTag("intention-input"))
  Button(onClick={focus.clearFocus(true);keyboard?.hide();details=false;revealResult=true;vm.quickIntention(text,hours,purpose)},enabled=text.isNotBlank()&&!working&&!busy&&!chat.working,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("intention-submit")){Text(if(working)t("整理緊指引…","Preparing…")else t("畀我奇門指引","Give me Qimen guidance"))}
  Text(when(chat.mode){"CHATGPT"->t("按上面按鈕會將今次心願及盤面交畀 ChatGPT 解讀；位置及日誌不會傳送。","The button sends this intention and its charts to ChatGPT. Location and journals stay private.");"API"->t("按上面按鈕會將今次心願及盤面傳送至你設定嘅 AI 供應商。","The button sends this intention and its charts to your configured AI provider.");else->t("目前用本機指引，毋須網絡。登入 ChatGPT 可直接加 AI 解讀。","Offline guidance is active. Sign in to add AI interpretation here.")},style=MaterialTheme.typography.bodySmall)
  TextButton(onClick={account=true},modifier=Modifier.testTag("intention-account")){Text(if(chat.connected)t("ChatGPT 已連接・管理 AI","ChatGPT connected · manage AI")else t("連接 ChatGPT／Codex","Connect ChatGPT / Codex"))}
  if(working){LinearProgressIndicator(Modifier.fillMaxWidth());TextButton(onClick=vm::cancelQuick){Text(t("取消等候，保留本機指引","Cancel waiting, keep offline guidance"))}}
  if(message.isNotBlank())Text(message,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.testTag("intention-status"))
  if(plan!=null && question!=null){
   val goal=plan.goalSnapshot!!
   CardBlock(t("今次心願","This intention")){
    Text(goal.description.ifBlank{goal.title},style=MaterialTheme.typography.titleMedium)
    Text(displayTime(plan.createdAt,question.input.timezone)+" · "+zoneCity(question.input.timezone,English.current),style=MaterialTheme.typography.bodySmall)
    val guidance=plan.guidance
    if(guidance!=null){
     Text(t("AI 奇門解讀","AI Qimen interpretation"),style=MaterialTheme.typography.labelLarge)
     SelectionContainer{Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(guidance.summary);if(guidance.reason.isNotBlank())Text(guidance.reason)}}
     Text(guidance.model+t("・解讀文字供參考，行程以本機計算卡為準。"," · Interpretation only; the calculated itinerary below is authoritative."),style=MaterialTheme.typography.bodySmall)
    }else{
     val reading=remember(question.chart_id){TraditionalReading.from(question)}
     Text(t("本機盤面簡讀","Offline chart reading"),style=MaterialTheme.typography.labelLarge)
     Text(if(English.current)reading.themeEn else reading.themeZh)
     Text(t("已按起意時間起盤。下方直接列出本方法符合條件嘅方位行程。","Calculated for the intention time. The matching direction itinerary is below."))
    }
    if(chat.mode!="OFFLINE" && !working)TextButton(onClick={vm.retryIntentionAi(plan.id)},modifier=Modifier.testTag("intention-ai-retry")){Text(t("直接解讀呢個盤","Interpret this chart here"))}
   }
   if(selected!=null){
    val chart=data.charts.first{it.id==selected.chartId}.let{Codec.decodeFromString<Chart>(it.chartJson)}
    val palace=chart.palaces.single{it.index==selected.itinerary!!.palace}
    val expired=now>=Instant.parse(selected.plannedEnd)
    Box(Modifier.bringIntoViewRequester(resultFocus)){CardBlock(if(expired)t("呢個出發時段已過","This departure window has ended")else t("跟住咁做","Your direction guide")){
     Text("${palace.direction} ${Navigator.bearing(palace.index)}°",style=MaterialTheme.typography.displaySmall,modifier=Modifier.testTag("intention-direction"))
     Text(t("出發：","Depart: ")+displayTime(selected.plannedStart,chart.input.timezone)+" → "+displayTime(selected.plannedEnd,chart.input.timezone))
     Text(zoneCity(chart.input.timezone,English.current)+t("・以時段結束前出發計。"," · Depart before the end of this window."),style=MaterialTheme.typography.bodySmall)
     if(!expired){
      Text(t("① 到上面時段，由固定出發點向${palace.direction}方出行。","① During the window, travel ${palace.direction} from your fixed starting point."))
      Text(t("② 揀你知道可以到達嘅地方；打開指引可睇指南針。","② Choose a place you know is accessible; open the guide for a compass."))
      Text(t("③ 到場後可按自己安排返回，無需額外手勢或指定步數。","③ Return when convenient. No additional gesture or step count is specified."))
      ActionButton(t("打開方位指引","Open direction guide")){onPlan(goal.id)}
     }else Text(t("請再按「畀我奇門指引」，用新時間重新起盤。","Use Give me Qimen guidance again to calculate for the new time."))
    }}
   }else Box(Modifier.bringIntoViewRequester(resultFocus)){CardBlock(t("今次未有匹配嘅出行時段","No matching departure window")){
    Text(t("今次時間範圍未符合所用規則。唔代表心願做唔到；起念盤仍可交 AI 解讀。","No match in this time range. This does not mean your intention cannot happen; AI can still interpret the question chart."),modifier=Modifier.testTag("intention-no-match"))
   }}
   TextButton(onClick={details=!details}){Text(t("睇盤面同依據","Chart and reasoning"))}
   if(details)CardBlock(t("今次點樣取用","Method used")){
    Text(if(plan.directionRequest?.purpose=="HARMONY")t("按心願文字辨識為和合；搜尋人遁／玉女守門。","The intention was routed to harmony: Ren Dun / Jade Maiden.")else t("按一般出行法搜尋三奇配吉門；唔代表已具備呢類心願嘅完整專門占法。","Uses the general outing profile, not a complete specialized reading for every kind of intention."))
    selected?.let{a->Text(a.rationale);val c=Codec.decodeFromString<Chart>(data.charts.first{it.id==a.chartId}.chartJson);val p=c.palaces.first{it.index==a.itinerary!!.palace};Text("${p.index}宮・${p.door}・${p.star}・${p.deity}・天${p.heaven_stem}／地${p.earth_stem}")}
    Text(t("時家問事盤；來源《煙波釣叟歌》《遁甲演義》卷二。未有出生盤、全盤旺衰判斷、地圖選址或返回吉時。","Time-of-question chart; sourced from Yanbo Diaosou Ge and Dunjia Yanyi vol. 2. No birth chart, full strength synthesis, map destination or auspicious return calculation."))
    Text("${question.ruleset.id}/${question.ruleset.version}\n${question.chart_id}",style=MaterialTheme.typography.labelSmall)
   }
  }
  TextButton(onClick={advanced=!advanced},modifier=Modifier.testTag("intention-advanced")){Text(t("進階選項","Advanced options"))}
  if(advanced){
   TimeZoneSelector(zone){vm.settings(state.settings.copy(timezone=it))}
   Text(t("自動取用可改；『今晚』按所選時區 18:00 至午夜；『明日』按翌日。未指日期就查未來 24 小時。","Routing is editable. Tonight means 18:00 to midnight in your zone; tomorrow means the next local day. Otherwise search the next 24 hours."))
   FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(null,"HARMONY","OUTING").forEach{v->FilterChip(purpose==v,{purpose=v},label={Text(when(v){"HARMONY"->t("和合","Harmony");"OUTING"->t("出行","Outing");else->t("自動","Auto")})})}}
   FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(0,24,72,168).forEach{h->FilterChip(hours==h,{hours=h},label={Text(when(h){0->t("自動","Auto");168->t("7 日","7 days");else->t("$h 小時","$h hours")})},modifier=Modifier.testTag("quick-hours-$h"))}}
  }
  TextButton(onClick={history=true},modifier=Modifier.testTag("intention-history")){Text(t("進階／歷史","Advanced / history"))}
  Text(t("傳統參考；不保證心願實現。","Traditional reference; outcomes are not guaranteed."),style=MaterialTheme.typography.bodySmall)
 }
}

@Composable private fun ChatAccountScreen(vm:AppModel,state:ChatUiState,onBack:()->Unit,onSettings:()->Unit){
 val context=LocalContext.current
 fun openLogin(){runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(state.loginUrl)))}}
 LaunchedEffect(state.userCode){if(state.userCode.isNotBlank())openLogin()}
 Page(t("連接你嘅 AI","Connect your AI"),t("登入一次，之後直接喺天機睇解讀。","Sign in once; read interpretations in TianJi.")){
  TextButton(onClick=onBack){Text(t("返回心願","Back to intention"))}
  CardBlock("ChatGPT／Codex"){
   Text(t("沿用粵伴鍵盤嘅裝置碼登入方式。密碼只喺 OpenAI 登入頁輸入，天機獨立保存自己嘅授權。","Uses the keyboard's device-code flow. Enter your password only on OpenAI's sign-in page; TianJi stores its own grant."))
   if(state.connected){
    Text(t("已登入","Signed in"),style=MaterialTheme.typography.titleMedium)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(t("用 ChatGPT 解讀心願","Interpret with ChatGPT"),Modifier.weight(1f));Switch(state.mode=="CHATGPT",{vm.aiMode(if(it)"CHATGPT"else"OFFLINE")})}
    Text(t("啟用後，按「畀我奇門指引」會傳送當次心願同盤面。唔會傳位置、日誌或其他心願。","When enabled, submitting sends that intention and its charts, not location, journals or other intentions."),style=MaterialTheme.typography.bodySmall)
    Text(t("模型：","Model: ")+state.model.ifBlank{t("登入後自動選擇","Chosen after sign-in")})
    var modelsOpen by remember{mutableStateOf(false)}
    TextButton(onClick={modelsOpen=!modelsOpen}){Text(t("更改模型","Change model"))}
    if(modelsOpen)state.models.forEach{m->OutlinedButton(onClick={vm.selectModel(m.id);modelsOpen=false},modifier=Modifier.fillMaxWidth()){Text((if(m.id==state.model)"✓ "else "")+m.label)}}
    OutlinedButton(onClick=vm::refreshChatModels,enabled=!state.working){Text(t("更新模型清單","Refresh models"))}
    TextButton(onClick=vm::logoutChatGpt){Text(t("登出 ChatGPT","Sign out of ChatGPT"))}
   }else if(state.userCode.isBlank())ActionButton(t("登入 ChatGPT／Codex","Sign in to ChatGPT / Codex"),enabled=!state.working,onClick=vm::loginChatGpt)
   if(state.userCode.isNotBlank()){
    Text(t("喺登入頁輸入呢個代碼","Enter this code on the sign-in page"))
    SelectionContainer{Text(state.userCode,style=MaterialTheme.typography.headlineMedium)}
    OutlinedButton(onClick={val clip=ClipData.newPlainText("ChatGPT device code",state.userCode);clip.description.extras=PersistableBundle().apply{putBoolean("android.content.extra.IS_SENSITIVE",true)};(context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)}){Text(t("複製登入代碼","Copy sign-in code"))}
    ActionButton(t("開啟 OpenAI 登入頁","Open OpenAI sign-in"),onClick=::openLogin)
    Text(t("授權後返天機，App 會自動完成連接。代碼過期可重新登入。","Return after authorizing; the app finishes connecting. Restart sign-in if the code expires."))
   }
   if(state.working){LinearProgressIndicator(Modifier.fillMaxWidth());TextButton(onClick=vm::cancelAccount){Text(t("取消連接","Cancel connection"))}}
   ErrorText(state.error)
   Text(t("模型及用量由你帳戶決定。登入成功唔等於每個模型都獲准使用。","Models and usage depend on your account. Sign-in alone does not establish access to every model."),style=MaterialTheme.typography.bodySmall)
  }
  CardBlock(t("已有 OpenAI API 設定？","Already configured an OpenAI API?")){
   Text(t("亦可用設定內嘅 HTTPS API、金鑰及模型直接解讀；費用按你嘅供應商帳戶計。","You can use the HTTPS API, key and model in Settings. Provider account charges apply."))
   OutlinedButton(onClick={vm.aiMode("API");onBack()}){Text(t("用已設定 API","Use configured API"))}
   TextButton(onClick=onSettings){Text(t("前往 API 設定","API settings"))}
  }
  TextButton(onClick={vm.aiMode("OFFLINE");onBack()}){Text(t("繼續用本機指引","Continue offline"))}
 }
}
