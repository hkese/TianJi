package com.tianji.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable fun GuideScreen(onGoals:()->Unit,onSettings:()->Unit,onBack:()->Unit){
 Page(t("使用教學","Getting started"),t("寫低心願，跟住時段同方位行動。","Set an intention and follow time and direction.")){
  TextButton(onClick=onBack){Text(t("返回","Back"))}
  CardBlock(t("目前可以做甚麼？","What works today?")){
   Text(t("離線起盤、按古籍簡式搜尋時段及方位、逐步記錄出發到返回。一般出行用奇門相合；和合相處用人遁／玉女守門。毋須帳戶、出生資料或 AI。","Calculate offline, find times and directions using a documented classical profile, and record departure through return. No account, birth data or AI is required."))
   Text(t("呢版唔會再將寫便條或做小樣當成奇門行程。候選有真實盤及規則依據，但唔保證心願實現。","Each itinerary has its actual chart and rule evidence. Results are not guaranteed."))
  }
  CardBlock(t("1 · 揀時區，寫心願","1 · Time zone and intention")){
   Text(t("到設定按時區，直接揀城市；西雅圖用「西雅圖／洛杉磯」，夏令時間自動處理。返首頁按「直接畀我指引」，唔寫任何嘢都可以查今日趨吉避凶。有特定事情先按「有一件事想問」，寫一句再查。","Choose a city in Settings. Seattle uses Seattle / Los Angeles, with automatic daylight saving. On Home, write an intention and tap Give me Qimen guidance. No goal form or method selection is required."))
   OutlinedButton(onClick=onSettings){Text(t("去設定時區","Choose time zone"))}
  }
  CardBlock(t("2 · 搜尋時段及方位","2 · Find a time and direction")){
   Text(t("首頁會自動起盤、取用同搜尋，直接顯示一份指引。想改時區、用途或搜尋範圍，可以展開「進階選項」；原有多方案流程保留喺「進階／歷史」。","Home calculates and routes automatically, then shows one guide. Advanced options change the zone, purpose or range; older workflows remain under Advanced / history."))
   Text(t("搵唔到符合條件就會講明，可擴大範圍。唔會勉強配方向；亦唔代表心願做唔到。候選時段指『出發』，最後一刻不包括。","No match is shown honestly; expand the range if needed. It does not mean your intention is impossible. Windows apply to departure and exclude their end instant."))
  }
  CardBlock(t("3 · 揀行程，按指引做","3 · Follow the itinerary")){
   Text(t("揀一個候選後，頂部會顯示目前階段。手機平放睇指南針；由固定起點，向所選方位揀一個實際可行嘅目的地。到時按「開始出發」，到場按「我已到達」。","Choose a candidate to pin your current stage. Hold the phone flat to read the compass and select an accessible destination in the direction from a fixed starting point. Tap Start walking in the window, then I have arrived."))
   Text(t("停留 不另停留或 5／10／15 分鐘係你揀嘅行程安排，唔係古籍吉數。計時到可返程，需要時亦可提早。返到起點按完成，之後另記心願有咩變化。","No extra stay or a 5, 10 or 15-minute stay is your schedule, not a ritual number. Return when the timer ends or earlier if needed. Mark completion, then separately record changes to your intention."))
   Text(t("原出發時段過咗就不能新開始。已開始嘅行程可以繼續記錄；App 重開會保留階段。","An expired window cannot start a new journey. An already started journey can continue, and its stage survives app restarts."))
  }
  CardBlock(t("定位、方向同道路","Location, compass and roads")){
   Text(t("定位係選用：可記起點同到場位置，比較相對方向及距離。無定位時指南針顯示磁北；有位置先作真北修正。未有足夠精度或感應器會講明。","Location is optional. Save start and arrival fixes to compare bearing and distance. Without a fix the compass reads magnetic north; a fix enables true-north correction. Missing sensors and insufficient accuracy are shown."))
   Text(t("App 未有地圖道路導航、自動城市選址、返程吉時或回家坐向；唔好將箭嘴當成可直行路線。只看方位模式不用出行，但唔等於完成古籍出行法。","There is no street routing, automatic city selection, auspicious return time or home sitting direction. A bearing is not a traversable route. View-only mode is not performance of a traditional outing."))
   Text(t("『讀出呢一步』用手機已安裝嘅離線廣東話語音，唔會上傳文字。沒有語音時可在 Android 文字轉語音設定下載，文字指引照常用。","Read this step uses an installed offline voice and does not upload text. Install a voice in Android text-to-speech settings if needed; written guidance remains available."))
  }
  CardBlock(t("AI 解說與私隱","AI explanation and privacy")){
   Text(t("首頁按「連接 ChatGPT／Codex」，用裝置碼登入一次。之後按「直接畀我指引」，心願同盤面會直接交 AI，解讀直接顯示及保存喺天機。不傳 GPS、其他心願、金鑰或日誌；可隨時改用本機指引。","Connect ChatGPT / Codex from Home with device-code sign-in. Submitting then sends only that intention and charts, and saves the interpretation in TianJi. GPS, other intentions, keys and journals are excluded. Offline mode remains available."))
   Text(t("資料留喺手機。備份會包含你選擇記錄嘅位置，請當私人資料保管；匯入會取代現有記錄。0.6 可更新 0.4／0.5，舊版實用方案保留為歷史。","Data stays on the phone. Backups include locations you chose to record, so keep them private. Import replaces records. Version 0.6 updates 0.4 / 0.5 and preserves legacy practical plans."))
  }
  ActionButton(t("返首頁睇指引","Start with an intention"),onClick=onGoals)
 }
}
