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
import androidx.compose.ui.text.style.TextOverflow
import com.tianji.data.*
import com.tianji.domain.*
import com.tianji.home.HomeScreen
import com.tianji.qimen.*
import com.tianji.ui.*
import kotlinx.coroutines.delay
import java.time.*

@OptIn(ExperimentalLayoutApi::class,ExperimentalFoundationApi::class)
@Composable fun IntentionScreen(vm:AppModel,state:UiState,chat:ChatUiState,working:Boolean,message:String,busy:Boolean,onPlan:(String)->Unit,onChart:()->Unit,onGuide:()->Unit,onGoals:()->Unit,onSettings:()->Unit){
 var text by rememberSaveable{mutableStateOf("")};var custom by rememberSaveable{mutableStateOf(false)}
 var advanced by rememberSaveable{mutableStateOf(false)};var history by rememberSaveable{mutableStateOf(false)};var account by rememberSaveable{mutableStateOf(false)}
 var hours by rememberSaveable{mutableIntStateOf(0)};var purpose by rememberSaveable{mutableStateOf<String?>(null)}
 var details by rememberSaveable{mutableStateOf(false)};var readingOpen by rememberSaveable{mutableStateOf(false)}
 var revealResult by remember{mutableStateOf(false)};val resultFocus=remember{BringIntoViewRequester()}
 var now by remember{mutableStateOf(Instant.now())};LaunchedEffect(Unit){while(true){now=Instant.now();delay(1000)}}
 val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
 val data=state.data;val zone=state.settings.timezone
 if(history){Column{TextButton(onClick={history=false}){Text(t("返回心願","Back to intention"))};Box(Modifier.weight(1f)){HomeScreen(data,zone,onGoals,onPlan,onChart,onGuide)}};return}
 if(account){ChatAccountScreen(vm,chat,{account=false},onSettings);return}
 val plan=data.plans.filter{it.entryMode=="INTENTION"}.maxByOrNull{it.createdAt}
 val selected=data.actions.firstOrNull{it.planVersionId==plan?.id && it.selected}
 val snapshot=data.charts.firstOrNull{it.id==plan?.questionChartId}
 val question=remember(snapshot?.id){snapshot?.let{Codec.decodeFromString<Chart>(it.chartJson)}}
 LaunchedEffect(plan?.id){if(plan!=null && revealResult){withFrameNanos{};resultFocus.bringIntoView();revealResult=false}}
 Page(t("今日奇門指引","Your Qimen guide"),t("睇清楚下一步，再跟住做。","See your next step, then follow the guide.")){
  Text(zoneCity(zone,English.current),style=MaterialTheme.typography.labelLarge)
  if(custom)OutlinedTextField(value=text,onValueChange={if(it.length<=1000)text=it},label={Text(t("我想……","I'd like…"))},placeholder={Text(t("例如：希望今晚相處和氣","For example: a harmonious evening"))},minLines=2,maxLines=5,modifier=Modifier.fillMaxWidth().testTag("intention-input"))
  Button(onClick={focus.clearFocus(true);keyboard?.hide();details=false;readingOpen=false;revealResult=true;vm.quickIntention(DirectGuide.intention(if(custom)text else ""),hours,purpose)},enabled=(!custom || text.isNotBlank())&&!working&&!busy&&!chat.working,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("intention-submit")){Text(if(working)t("整理緊指引…","Preparing…")else t("直接畀我指引","Guide me now"))}
  TextButton(onClick={custom=!custom},modifier=Modifier.testTag("intention-custom")){Text(if(custom)t("改用今日趨吉避凶","Use today's general guide")else t("有一件事想問？寫一句就得","Have something specific in mind?"))}
  Text(when(chat.mode){"CHATGPT"->t("按上面按鈕，會將今次心願同盤面交 ChatGPT 解讀。","Submitting sends this intention and its charts to ChatGPT.");"API"->t("按上面按鈕，會將今次心願同盤面交你設定嘅 AI 解讀。","Submitting sends this intention and its charts to your AI provider.");else->t("本機已可起盤同出指引，毋須網絡。","Charts and guidance work offline.")},style=MaterialTheme.typography.bodySmall)
  if(working){LinearProgressIndicator(Modifier.fillMaxWidth());TextButton(onClick=vm::cancelQuick){Text(t("取消等候，保留本機指引","Cancel waiting, keep offline guidance"))}}
  if(message.isNotBlank())Text(message,style=MaterialTheme.typography.bodySmall,modifier=Modifier.testTag("intention-status"))
  if(plan!=null && question!=null){
   val goal=plan.goalSnapshot!!
   if(selected!=null){
    val source=data.charts.first{it.id==selected.chartId}
    val chart=remember(source.id){Codec.decodeFromString<Chart>(source.chartJson)}
    val guide=DirectGuide.from(selected,chart,now,English.current)
    Box(Modifier.bringIntoViewRequester(resultFocus)){CardBlock(t("下一步","Your next step")){
     Text(goal.description.ifBlank{goal.title},style=MaterialTheme.typography.labelLarge,maxLines=2,overflow=TextOverflow.Ellipsis,modifier=Modifier.testTag("guide-intention"))
     Text(when(guide.state){"WAIT"->t("先等到下方時段，暫時未出發","Wait for the window below");"READY"->t("而家喺出發時段內","You can depart during this window");"ACTIVE"->t("行程進行中，繼續跟返呢一步","Your journey is in progress");"DONE"->t("行程已完成，記低之後有咩變化","Journey completed; record what happened");else->t("呢份指引已過時，請重新整理","This guide has expired; refresh it")},style=MaterialTheme.typography.titleMedium,modifier=Modifier.testTag("guide-next-step"))
     if(guide.state!="EXPIRED" && guide.state!="DONE"){
      Text("${guide.direction} ${guide.bearing}°",style=MaterialTheme.typography.displaySmall,modifier=Modifier.testTag("intention-direction"))
      Text(guide.window,style=MaterialTheme.typography.titleMedium)
      Text(zoneCity(chart.input.timezone,English.current)+t("・呢個係出發時段"," · Departure window"),style=MaterialTheme.typography.bodySmall)
      if(guide.state=="WAIT" || guide.state=="READY"){
       Text(t("到時由固定起點，向${guide.direction}方行去你熟悉、可以到達嘅地方。","During that window, travel ${guide.direction} from a fixed starting point to an accessible place you know."))
       Text(t("到場後可自行安排返回，再記低心願有咩變化。","Return when convenient and record what happened."),style=MaterialTheme.typography.bodySmall)
      }
      ActionButton(if(guide.state=="ACTIVE")t("繼續行程","Continue journey")else t("開指南針，跟住行","Open compass and follow")){onPlan(goal.id)}
     }else if(guide.state=="DONE")ActionButton(t("記低心願結果","Record the outcome")){onPlan(goal.id)}
     else Text(t("按上面「直接畀我指引」，用現在時間重新起盤。","Tap Guide me now to calculate a fresh chart."))
    }}
    if(guide.avoid.isNotEmpty())CardBlock(t("同一時段先避用","Avoid for that same window")){
     Text(guide.avoid.joinToString("、"){it.direction},style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("guide-avoid"))
     Text(t("今次出行先唔選上面方位；只適用於上方時段。","Do not select those directions for this outing; this applies only to the window above."))
     Text(t("按所用奇門簡式篩選，唔代表全天凶方或一定出事。","A limited traditional screening, not an all-day danger forecast."),style=MaterialTheme.typography.bodySmall)
    }
   }else Box(Modifier.bringIntoViewRequester(resultFocus)){CardBlock(t("下一步：今次先唔按此法出行","Next step: no outing selected")){
    Text(goal.description.ifBlank{goal.title},modifier=Modifier.testTag("guide-intention"))
    Text(t("今次搜尋範圍未有符合所用規則嘅時段，先唔揀出行方向。唔代表今日會出事。","No window matches the selected rules and range. No direction is selected; this does not mean something bad will happen."),modifier=Modifier.testTag("intention-no-match"))
    if(goal.title==DirectGuide.DAILY_INTENTION){
     ActionButton(t("睇下一個可用時段","Find the next available window"),enabled=!working&&!busy){revealResult=true;vm.quickIntention("趨吉避凶",168,plan.directionRequest!!.purpose)}
     Text(t("改查未來 7 日；會寫清楚實際日期。","Searches the next 7 days and shows the actual date."),style=MaterialTheme.typography.bodySmall)
    }else Text(t("可改心願日期再查，或在進階選項擴大範圍。","Change the intention date or extend the range in Advanced."))
   }}
   CardBlock(t("今次心願","This intention")){
    Text(displayTime(plan.createdAt,question.input.timezone)+" · "+zoneCity(question.input.timezone,English.current),style=MaterialTheme.typography.bodySmall)
    val guidance=plan.guidance
    if(guidance!=null){Text(t("AI 奇門解讀","AI Qimen interpretation"),style=MaterialTheme.typography.labelLarge);SelectionContainer{Text(guidance.summary)};if(readingOpen && guidance.reason.isNotBlank())Text(guidance.reason)}
    else Text(t("上面先列本機計好嘅行動指引。","Your calculated next step is shown above."))
    if(chat.mode!="OFFLINE" && !working)TextButton(onClick={vm.retryIntentionAi(plan.id)},modifier=Modifier.testTag("intention-ai-retry")){Text(if(guidance==null)t("加 AI 解讀","Add AI interpretation")else t("更新 AI 解讀","Refresh AI interpretation"))}
    TextButton(onClick={readingOpen=!readingOpen}){Text(t("點解係咁？","Why this guide?"))}
    if(readingOpen){
     if(guidance==null){val reading=remember(question.chart_id){TraditionalReading.from(question)};Text(if(English.current)reading.themeEn else reading.themeZh)}
     guidance?.let{Text(it.model+t("・AI 解讀供參考，執行時間同方向以上方卡為準。"," · Interpretation only; follow the calculated card above."),style=MaterialTheme.typography.bodySmall)}
    }
   }
   TextButton(onClick={details=!details}){Text(t("睇盤面同依據","Chart and reasoning"))}
   if(details)CardBlock(t("今次點樣取用","Method used")){
    Text(if(plan.directionRequest?.purpose=="HARMONY")t("今次採用和合簡式：人遁／玉女守門。","Harmony profile: Ren Dun / Jade Maiden.")else t("今次採用一般出行簡式：三奇配吉門。","General outing profile: Qi-Men conjunction."))
    selected?.let{a->
     val c=Codec.decodeFromString<Chart>(data.charts.first{it.id==a.chartId}.chartJson);val p=c.palaces.first{it.index==a.itinerary!!.palace}
     Text(a.rationale);Text("${p.index}宮・${p.door}・${p.star}・${p.deity}・天${p.heaven_stem}／地${p.earth_stem}")
     DirectionRules.decisions(c,a.itinerary!!.purpose).filter{it.excluded.isNotEmpty()}.forEach{d->Text("${c.palaces.single{it.index==d.palace}.direction}（${d.palace}宮）：${d.excluded.joinToString("、")}")}
     Text(t("以上避用資料只屬此行程的出發時段。","These exclusions belong only to this departure window."))
    }
    Text(t("時家問事盤；來源《煙波釣叟歌》《遁甲演義》卷二。未有完整旺衰、出生盤、地圖選址、手勢步數或返回吉時。","Time-of-question chart. Limited sourced method; no full synthesis, birth chart, map destination, gestures or auspicious return time."))
    Text("${question.ruleset.id}/${question.ruleset.version}\n${question.chart_id}",style=MaterialTheme.typography.labelSmall)
   }
  }
  TextButton(onClick={account=true},modifier=Modifier.testTag("intention-account")){Text(if(chat.connected)t("ChatGPT 已連接・管理 AI","ChatGPT connected · manage AI")else t("連接 ChatGPT／Codex","Connect ChatGPT / Codex"))}
  TextButton(onClick={advanced=!advanced},modifier=Modifier.testTag("intention-advanced")){Text(t("時區及進階選項","Time zone and advanced options"))}
  if(advanced){
   TimeZoneSelector(zone){vm.settings(state.settings.copy(timezone=it))}
   Text(t("『今日』只查所選時區今日餘下時間。想查之後，寫明日期或移除『今日』再改範圍。","Today is bounded by local midnight. For later windows, change the intention date or omit today and extend the range."))
   FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(null,"HARMONY","OUTING").forEach{v->FilterChip(purpose==v,{purpose=v},label={Text(when(v){"HARMONY"->t("和合","Harmony");"OUTING"->t("出行","Outing");else->t("自動","Auto")})})}}
   FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(0,24,72,168).forEach{h->FilterChip(hours==h,{hours=h},label={Text(when(h){0->t("自動","Auto");168->t("7 日","7 days");else->t("$h 小時","$h hours")})},modifier=Modifier.testTag("quick-hours-$h"))}}
  }
  TextButton(onClick={history=true},modifier=Modifier.testTag("intention-history")){Text(t("進階／歷史","Advanced / history"))}
  Text(t("傳統參考；不保證趨吉避凶效果。","Traditional reference; outcomes are not guaranteed."),style=MaterialTheme.typography.bodySmall)
 }
}

@Composable private fun ChatAccountScreen(vm:AppModel,state:ChatUiState,onBack:()->Unit,onSettings:()->Unit){
 val context=LocalContext.current
 var lastBrowserRequest by rememberSaveable{mutableLongStateOf(0)}
 fun openLogin(){runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(state.loginUrl)))}}
 LaunchedEffect(state.browserRequest){if(state.browserRequest!=0L && state.browserRequest!=lastBrowserRequest && state.userCode.isNotBlank()){lastBrowserRequest=state.browserRequest;openLogin()}}
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
   if(state.working){LinearProgressIndicator(Modifier.fillMaxWidth());TextButton(onClick={vm.cancelAccount()}){Text(t("取消連接","Cancel connection"))}}
   if(state.loginStatus.isNotBlank())Text(state.loginStatus,modifier=Modifier.testTag("login-status"))
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
