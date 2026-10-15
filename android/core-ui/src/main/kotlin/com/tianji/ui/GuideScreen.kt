package com.tianji.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable fun GuideScreen(onGoals:()->Unit,onSettings:()->Unit,onBack:()->Unit){
 Page(t("使用教學","Getting started"),t("一撳出指引，跟住下一步做。","One tap for your next step.")){
  TextButton(onClick=onBack){Text(t("返回","Back"))}
  CardBlock(t("目前可以做甚麼？","What works today?")){
   Text(t("按奇門出行簡式，列出出發時段、方位，同一時段先避用嘅方向。毋須識排盤或揀三個方案；本機離線都用到。","See a departure window, direction and directions excluded by the same traditional profile. You do not need to read a chart or choose three options. Offline guidance works too."))
  }
  CardBlock(t("1 · 撳「直接畀我指引」","1 · Tap Guide me now")){
   Text(t("首頁唔使填任何嘢，直接查今日趨吉避凶。有特定事情，先按「有一件事想問」，寫一句再查。","On Home, leave the form closed for today's general guide. For a specific matter, tap the intention link and write one sentence."))
   Text(t("今日冇匹配時段，就按「睇下一個可用時段」。App 會寫清楚實際日期，唔會將聽日當今日。","If there is no match today, look for the next available window. Its actual date is shown."))
  }
  CardBlock(t("2 · 睇「下一步」","2 · Read Your next step")){
   Text(t("見到「先等到」就等到顯示時段；見到「而家喺出發時段內」先可以依呢份指引出發。過時就按上面按鈕重新整理。","Wait when told to wait. Depart within the displayed window. Refresh an expired guide."))
   Text(t("下面「同一時段先避用」只適用呢次出行時段，唔係話嗰啲方位全天都凶或一定出事。想知原因，按「睇盤面同依據」。","The exclusions apply only to that departure window. They are not all-day danger forecasts. Open the reasoning for their chart basis."))
  }
  CardBlock(t("3 · 開指南針，跟住行","3 · Open the compass and follow")){
   Text(t("按「開指南針，跟住行」。行程已幫你選好，毋須再揀方案。手機平放，由固定起點向顯示方位，揀一個你熟悉、可到達嘅地方。","Open the compass. Your itinerary is already selected. Hold the phone flat and choose an accessible place you know in that direction from a fixed starting point."))
   Text(t("到時按「開始出發」，到場按「我已到達」，之後按自己安排返回。完成後記低心願有咩變化。指南針唔係地圖道路導航。","Start the journey in the departure window, mark arrival, then return when convenient. Record what happened afterward. The compass is not street navigation."))
  }
  CardBlock(t("AI 同時區","AI and time zone")){
   Text(t("ChatGPT 已連接就可以直接用，毋須每次登入或複製提示詞。AI 解讀顯示喺行動卡下面；未有網絡仍可先睇本機指引。","A connected ChatGPT account can interpret in the app without repeated login or copied prompts. Its text appears below the action card. Offline guidance remains available."))
   Text(t("首頁會顯示所用城市。西雅圖揀「西雅圖／洛杉磯」，夏令時間自動處理，唔使輸入時區代碼。","Home shows the selected city. Choose Seattle / Los Angeles for Pacific time, with automatic daylight saving."))
   OutlinedButton(onClick=onSettings){Text(t("去設定時區","Choose time zone"))}
   Text(t("舊記錄喺「進階／歷史」；資料預設留喺手機。只有按指引按鈕並啟用 AI，先傳送當次心願同盤面，唔傳 GPS、日誌或其他心願。","Find older records in Advanced / history. Data stays on your phone by default. Submitting with AI enabled sends only that intention and its charts, not GPS, journals or other intentions."))
  }
  Text(t("奇門屬傳統參考，唔保證效果。","Traditional reference; outcomes are not guaranteed."))
  ActionButton(t("返首頁睇指引","Go to your guide"),onClick=onGoals)
 }
}
