package com.tianji.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.tianji.domain.*
import com.tianji.ui.*
import kotlinx.coroutines.delay
import java.time.*

@Composable fun HomeScreen(data:Backup,zone:String,onGoals:()->Unit,onPlan:(String)->Unit,onChart:()->Unit,onGuide:()->Unit){
 var now by remember { mutableStateOf(Instant.now()) }
 LaunchedEffect(zone) { while(true) { now=Instant.now(); delay(30_000) } }
 Page(t("留意當下。\n行好下一步。","Be here.\nTake your next step."),displayTime(now.toString(),zone)+" · "+zoneCity(zone,English.current)){
  CardBlock(t("今日方向","Today's focus")){
   val goal=data.goals.filter{it.status=="ACTIVE"}.sortedByDescending{it.priority}.firstOrNull()
   if(goal==null){Text(t("由一個具體目標開始。你的資料留在手機。","Begin with a concrete goal. Your data stays on your phone."));ActionButton(t("建立第一個目標","Create your first goal"),onClick=onGoals)}
   else{Text(goal.title,style=MaterialTheme.typography.headlineSmall);Text(goal.successCriteria);val latest=data.plans.filter{it.goalId==goal.id}.maxByOrNull{it.createdAt};val next=data.actions.firstOrNull{it.planVersionId==latest?.id && it.selected};Text(next?.let{actionTitle(it.kind,it.title)}?:t("比較三個可行步驟，再作選擇。","Compare three practical next steps."));ActionButton(t("檢視下一步","Review next step")){onPlan(goal.id)}}
  }
  CardBlock(t("第一次用？由呢度開始","New here? Start here")){
   Text(t("建立目標 → 揀一個方案 → 記錄結果 → 調整計劃","Create a goal → Choose an action → Record the result → Adjust"))
   OutlinedButton(onClick=onGuide){Text(t("使用教學","Getting started"))}
  }
  CardBlock(t("奇門排盤尚未開放","Qimen calculation is not available yet")){
   Text(t("現時可使用離線行動規劃。奇門規則及參考盤尚未核實，並未提供排盤或吉時預測。","Offline action planning is available. Qimen rules and reference charts remain unverified; charts and auspicious-time predictions are unavailable."))
   TextButton(onClick=onChart){Text(t("了解功能狀態","View feature status"))}
  }
  Evidence()
 }
}
