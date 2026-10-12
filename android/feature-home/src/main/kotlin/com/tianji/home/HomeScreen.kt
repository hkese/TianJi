package com.tianji.home
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tianji.domain.*
import com.tianji.ui.*
import java.time.*
@Composable fun HomeScreen(data:Backup,zone:String,onGoals:()->Unit,onPlan:(String)->Unit,onChart:()->Unit){
 Page(t("留意當下。\n行好下一步。","Be here.\nTake your next step."),displayTime(Instant.now().toString(),zone)+" · "+zone){
  CardBlock(t("今日方向","Today's focus")){
   val goal=data.goals.filter{it.status=="ACTIVE"}.sortedByDescending{it.priority}.firstOrNull()
   if(goal==null){Text(t("由一個具體目標開始。你的資料留在手機。","Begin with a concrete goal. Your data stays on your phone."));ActionButton(t("建立第一個目標","Create your first goal"),onClick=onGoals)}
   else{Text(goal.title,style=MaterialTheme.typography.headlineSmall);Text(goal.successCriteria);val latest=data.plans.filter{it.goalId==goal.id}.maxByOrNull{it.createdAt};val next=data.actions.firstOrNull{it.planVersionId==latest?.id && it.selected};Text(next?.title?:t("比較三個可行步驟，再作選擇。","Compare three practical next steps."));ActionButton(t("檢視下一步","Review next step")){onPlan(goal.id)}}
  }
  CardBlock(t("奇門 Core · 待驗證","Qimen Core · Awaiting verification")){Text("shijia-zhuanpan-chaibu · 0.0.1-blocked");Text(t("獨立參考盤：0 / 100。尚未計算奇門符號。","Independent reference charts: 0 / 100. Qimen symbols are not calculated."));OutlinedButton(onClick=onChart){Text(t("查看計算狀態","View calculation status"))}}
  Evidence()
 }
}
