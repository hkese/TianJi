package com.tianji.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.tianji.domain.*
import com.tianji.qimen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tianji.ui.*
import kotlinx.coroutines.delay
import java.time.*

@Composable fun HomeScreen(data:Backup,zone:String,onGoals:()->Unit,onPlan:(String)->Unit,onChart:()->Unit,onGuide:()->Unit){
 var now by remember { mutableStateOf(Instant.now()) }
 LaunchedEffect(zone) { while(true) { now=Instant.now(); delay(30_000) } }
 val core=remember{QimenCore()}
 val chart by produceState<Chart?>(null,zone,now){value=withContext(Dispatchers.Default){runCatching{core.calculateChart(now,zone)}.getOrNull()}}
 Page(t("留意當下。\n行好下一步。","Be here.\nTake your next step."),displayTime(now.toString(),zone)+" · "+zoneCity(zone,English.current)){
  CardBlock(t("今日方向","Today's focus")){
   val goal=data.goals.filter{it.status=="ACTIVE"}.sortedByDescending{it.priority}.firstOrNull()
   if(goal==null){Text(t("由一個具體目標開始。你的資料留在手機。","Begin with a concrete goal. Your data stays on your phone."));ActionButton(t("建立第一個目標","Create your first goal"),onClick=onGoals)}
   else{Text(goal.title,style=MaterialTheme.typography.headlineSmall);Text(goal.successCriteria);val latest=data.plans.filter{it.goalId==goal.id}.maxByOrNull{it.createdAt};val next=data.actions.firstOrNull{it.planVersionId==latest?.id && it.selected};Text(next?.let{if(English.current)it.guide?.titleEn?:actionTitle(it.kind,it.title) else it.title}?:t("比較三個可行步驟，再作選擇。","Compare three practical next steps."));next?.guide?.let{guide->Text(t("即刻做：","Start here: ")+(if(English.current)guide.stepsEn.first() else guide.stepsZh.first()))};ActionButton(t("檢視下一步","Review next step")){onPlan(goal.id)}}
  }
  CardBlock(t("即刻起個基本盤","Your offline chart")){
   chart?.let{c->
    val reading=remember(c){TraditionalReading.from(c)}
    Text("${c.dun_type}${c.ju_number}局 · ${c.calendar["solar_term"]}${c.calendar["yuan"]}",style=MaterialTheme.typography.titleMedium)
    Text(if(English.current)reading.themeEn else reading.themeZh,style=MaterialTheme.typography.headlineSmall)
   }
   Text(t("傳統推演作參考，唔係已證實嘅未來預測。毋須出生資料或 AI。","Traditional guidance, not a scientifically validated forecast. No birth details or AI needed."))
   ActionButton(t("起盤・睇中文簡讀","Open chart and reading"),onClick=onChart)
  }
  CardBlock(t("第一次用？由呢度開始","New here? Start here")){
   Text(t("建立目標 → 揀一個方案 → 記錄結果 → 調整計劃","Create a goal → Choose an action → Record the result → Adjust"))
   OutlinedButton(onClick=onGuide){Text(t("使用教學","Getting started"))}
  }
  Evidence()
 }
}
