package com.tianji.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.tianji.domain.*
import com.tianji.qimen.*
import com.tianji.ui.*
import java.time.Instant
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay

@Composable fun PlannerScreen(goalId:String,data:Backup,zone:String,onNew:(String)->Unit,onChoose:(ActionCandidate)->Unit,onJournal:(ActionCandidate)->Unit,onAi:(Goal,List<ActionCandidate>)->Unit,onBack:()->Unit){
 val goal=data.goals.firstOrNull{it.id==goalId}?:return
 val plans=data.plans.filter{it.goalId==goalId}.sortedByDescending{it.createdAt}
 var shown by rememberSaveable(goalId){mutableStateOf<String?>(null)}
 val plan=plans.firstOrNull{it.id==shown}?:plans.firstOrNull()
 var reason by rememberSaveable(goalId){mutableStateOf("")}
 var handoff by remember{mutableStateOf<String?>(null)}
 val actions=data.actions.filter{it.planVersionId==plan?.id}
 val snapshot=data.charts.firstOrNull{it.id==actions.firstOrNull()?.chartId}
 val chart=remember(snapshot){snapshot?.let{runCatching{Codec.decodeFromString<Chart>(it.chartJson)}.getOrNull()}}
 val chosen=actions.firstOrNull{it.selected}
 val focusManager=LocalFocusManager.current
 val keyboard=LocalSoftwareKeyboardController.current
 var clock by remember{mutableStateOf(Instant.now())}
 LaunchedEffect(Unit){while(true){clock=Instant.now();delay(30_000)}}
 val expired=actions.firstOrNull()?.let{Instant.parse(it.plannedEnd)<clock}?:false
 val scroll=rememberScrollState()
 val canPlan=goal.deadline?.let{Instant.parse(it)>=clock.plusSeconds(300)}?:true
 LaunchedEffect(chosen?.id){if(chosen!=null)scroll.animateScrollTo(0)}
 val initialReason=t("首次計劃","Initial plan")
 Page(t("下一步導航","Your next step"),goal.title,scrollState=scroll){
  TextButton(onClick=onBack){Text(t("返回目標","Back to goals"))}
  if(!canPlan)ErrorText(t("限期剩餘不足五分鐘。請返回目標，編輯限期後再規劃。","Less than five minutes remain. Go back and edit the deadline before planning."))
  if(plan==null){
   CardBlock(t("由你想做嘅事開始","Start from your purpose")){
    Text(t("想達成：","Goal: ")+goal.title)
    Text(t("完成標準：","Success criteria: ")+goal.successCriteria)
    Text(t("按下面按鈕，App 會用現在時間起盤，將三種做法、盤面方位同實際步驟放埋一齊。揀一個便可。","The button calculates a chart for now and links it to three practical approaches, symbolic directions and steps. Choose one."))
    Text(t("每次 ${goal.constraints.minutes} 分鐘 · 時區：","${goal.constraints.minutes} minutes · Time zone: ")+zoneCity(zone,English.current))
    ActionButton(t("產生三個方案","Generate three options"),enabled=canPlan){onNew(initialReason)}
   }
  } else {
   if(expired)CardBlock(t("原定時段已過","The original window has ended")){
    Text(t("下面保留當時的盤同建議。要而家行動，先重新起盤；舊方位唔會自動當成現在方位。","The saved chart and advice remain as history. Refresh before acting now; old directions are not current readings."))
    val refresh=t("原時段已過，按現在情況重新規劃","Original window ended; refresh for now")
    ActionButton(t("用現在時間重新規劃","Refresh for now"),enabled=canPlan){shown=null;onNew(refresh)}
   }
   chosen?.guide?.let{guide->
    CardBlock(t("你已揀好 · 即刻做呢一步","Your choice · start here")){
     Text(if(English.current)guide.titleEn else chosen.title,style=MaterialTheme.typography.titleMedium)
     Text(if(English.current)guide.stepsEn.first() else guide.stepsZh.first())
     Text(displayTime(chosen.plannedStart,snapshot?.timezone?:zone)+" → "+displayTime(chosen.plannedEnd,snapshot?.timezone?:zone))
     chart?.let{Navigator.palace(it,chosen)}?.let{p->Text(t("可選方向：${p.direction}（${p.door}）","Optional direction: ${p.direction} (${p.door})"))}
     Text(t("做完點算：","Check: ")+(if(English.current)guide.doneEn else guide.doneZh))
     val previous=data.journal.lastOrNull{it.actionId==chosen.id}
     if(previous!=null)Text(t("已記錄結果，可按新觀察重新規劃。","Outcome saved. Replan from your new observation."))
     ActionButton(t("記錄結果","Record outcome")){onJournal(chosen)}
    }
   }
   if(chart!=null){
    CardBlock(t("今次用邊個盤？","Which chart is this based on?")){
     Text(displayTime(chart.input.instant_utc,chart.input.timezone)+" · "+zoneCity(chart.input.timezone,English.current))
     Text("${chart.dun_type}${chart.ju_number}局 · ${chart.calendar["day_ganzhi"]}日 ${chart.calendar["hour_ganzhi"]}時")
     val focus=remember(chart){runCatching{TraditionalReading.from(chart)}.getOrNull()}
     focus?.let{Text(t("事情觀察點：時干 ${it.stem} 在 ${it.palace.index} 宮（${it.palace.direction}），${it.palace.door}。","Matter focus: hour stem ${it.stem}, palace ${it.palace.index} (${it.palace.direction}), ${it.palace.door}."))}
     Text(t("取用說明：以下每種做法以相應八門作簡化象意參考，再讀取本盤實際方位；唔係完整用神吉凶占斷。","Method: each practical approach uses a disclosed door as a simple symbolic lens and reads its actual palace. This is not a full divination judgment."),style=MaterialTheme.typography.bodySmall)
     Text(t("值符：${chart.calendar["duty_star"]} · 值使：${chart.calendar["duty_door"]}","Duty star: ${chart.calendar["duty_star"]} · Duty door: ${chart.calendar["duty_door"]}"))
    }
   }
   Text(t("三個做法，揀一個","Three approaches · choose one"),style=MaterialTheme.typography.headlineSmall)
   Text(t("時段按你的可用時間安排，並非吉時。先核對文字限制；做唔到就記低卡點，唔使勉強完成。","The window follows your availability, not an auspicious-time forecast. Check your written constraints; record an obstacle if blocked."))
   val frozen=plan.goalSnapshot?:goal
   if(frozen.constraints.notes.isNotBlank())Text(t("你的限制：","Your constraints: ")+frozen.constraints.notes)
   actions.forEachIndexed{i,a->
    val guide=a.guide
    CardBlock("${i+1} · "+(if(English.current)guide?.titleEn?:actionTitle(a.kind,a.title) else a.title)){
     if(guide!=null){
      Text(t("適合：","Useful when: ")+(if(English.current)guide.fitEn else guide.fitZh))
      Text(t("幾時做","When"),style=MaterialTheme.typography.labelLarge)
      Text(displayTime(a.plannedStart,snapshot?.timezone?:zone)+" → "+displayTime(a.plannedEnd,snapshot?.timezone?:zone))
      chart?.let{Navigator.palace(it,a)}?.let{p->
       Text(t("可選象意方位：${p.direction} · ${Navigator.bearing(p.index)}°","Optional symbolic direction: ${p.direction} · ${Navigator.bearing(p.index)}°"),style=MaterialTheme.typography.titleMedium)
       Text(t("本盤 ${p.index} 宮｜${p.door}・${p.star}・${p.deity}","Palace ${p.index} | ${p.door} · ${p.star} · ${p.deity}"))
       Text(t("想配合方位，可用手機指南針辨識此方向，在安全、方便的位置做下面步驟；留在原位做亦可以。App 冇量度你位置，毋須特登去另一個地方。","If you choose to use the direction, identify it with your phone's compass and work somewhere safe and convenient. Staying where you are is fine. The app does not measure your location or require travel."),style=MaterialTheme.typography.bodySmall)
      }
      Text(t("照住做","How to do it"),style=MaterialTheme.typography.titleMedium)
      (if(English.current)guide.stepsEn else guide.stepsZh).forEachIndexed{n,step->Text("${n+1}. $step")}
      Text(t("今次完成證據：","Evidence for this attempt: ")+(if(English.current)guide.doneEn else guide.doneZh))
      Text(t("時段結束就檢視一次，再按「記錄結果」。這只是通往目標的一步，唔等於成個目標已完成。","Review once the window ends, then record the outcome. This is one step toward your goal, not completion of the whole goal."),style=MaterialTheme.typography.bodySmall)
      Text(t("支出：0 · 準備步驟 · 先用現有材料","Cost: 0 · Preparation step · Use existing resources"))
      Text(t("步驟：實用規劃 HEURISTIC｜門與方位：傳統象意 TRADITIONAL。門象參考《煙波釣叟歌》；目的配對為本 App 自訂，未作全盤吉凶排名。","Steps: HEURISTIC. Door and direction: TRADITIONAL symbolism (Song of the Fishing Elder). Purpose mapping is app-authored, not a whole-chart auspiciousness ranking."),style=MaterialTheme.typography.bodySmall)
     }else{
      Text(t("呢個係舊版通用方案。填寫新觀察再重新規劃，就會有具體步驟同盤面依據。","This is a legacy generic option. Replan to get specific steps and chart context."))
      Text(a.rationale)
     }
     ActionButton(if(a.selected)t("已選擇","Selected") else t("選擇此方案","Choose this option")){onChoose(a)}
     if(a.selected && guide==null)OutlinedButton(onClick={onJournal(a)}){Text(t("記錄結果","Record outcome"))}
    }
   }
   if(snapshot!=null && chart?.status=="TRADITIONAL" && actions.isNotEmpty())CardBlock(t("想分析得更貼身？","Want a more personal analysis?")){
    Text(t("將目標、限制、呢個完整盤同三個方案，交畀 ChatGPT／Codex 深入分析。你先檢視再複製，毋須喺 App 填 API 金鑰。","Review and copy your goal, constraints, this complete chart and the three options into ChatGPT or Codex. No API key is needed for this manual handoff."))
    ActionButton(t("交畀 ChatGPT／Codex 分析","Analyze with ChatGPT / Codex")){
     handoff=AnalysisPrompt.build(goal,plan,actions,snapshot,data.journal)
    }
    OutlinedButton(onClick={onAi(frozen,actions)}){Text(t("已設定供應商？只比較方案次序","Configured provider: compare option order"))}
   }
   Text(t("計劃版本：","Plan version: ")+plan.version+" · "+displayTime(plan.createdAt,zone));Text(plan.reason)
  }
  Field(reason,t("新觀察／改計劃原因","New observation / reason to replan"),{reason=it},single=false)
  ActionButton(t("重新規劃，保留歷史","Replan and preserve history"),enabled=reason.isNotBlank() && canPlan){focusManager.clearFocus(force=true);keyboard?.hide();onNew(reason);shown=null;reason=""}
  if(plans.isNotEmpty())CardBlock(t("歷史版本","Plan history")){plans.forEach{p->TextButton(onClick={shown=p.id}){Text(displayTime(p.createdAt,zone)+" · "+p.reason)}}}
 }
 handoff?.let{AnalysisHandoff(it){handoff=null}}
}
