package com.tianji.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun GuideScreen(onGoals: () -> Unit, onSettings: () -> Unit, onBack: () -> Unit) {
    Page(t("使用教學", "Getting started"), t("先起盤睇簡讀，再按實際情況行動。", "Calculate a chart, read it, then consider your real circumstances.")) {
        TextButton(onClick = onBack) { Text(t("返回", "Back")) }
        CardBlock(t("目前可以做甚麼？", "What works today?")) {
            Text(t("離線建立目標、比較三個實用方案、記錄結果，再按新觀察調整計劃。毋須登入、連網或設定 AI。", "Create goals offline, compare three practical options, record outcomes and adjust your plan. No login, internet or AI setup required."))
            Text(t("「奇門」可離線起盤及看傳統簡讀；「目標」的三個方案仍是按目標與限制作實用規劃。兩者均不保證未來結果。", "Qimen calculates charts and basic traditional readings offline. The three goal-planning options remain practical suggestions based on your constraints. Neither guarantees future outcomes."))
        }
        CardBlock(t("1 · 揀好時區", "1 · Choose your time zone")) {
            Text(t("到「設定」按時區，直接揀城市，或選「使用手機目前時區」。西雅圖選「西雅圖／洛杉磯」；香港選「香港」。夏令時間會自動換算。", "In Settings, tap the time zone and choose a city or your device's current zone. Seattle uses Seattle / Los Angeles. Daylight saving is automatic."))
            Text(t("更改時區只改變時間顯示及新計劃的時區，不會搬動已儲存行動的實際時間。旅行後可再次選擇手機時區。", "Changing the zone changes time displays and the zone for new plans, not saved actions' actual instants. When travelling, choose the device's zone again."))
            OutlinedButton(onClick = onSettings) { Text(t("去設定時區", "Choose time zone")) }
        }
        CardBlock(t("2 · 起一個最簡單的盤", "2 · Calculate a basic chart")) {
            Text(t("底部按「奇門」→「用現在時間起盤」。毋須出生日期、GPS、帳戶或 AI。想選其他時刻，就按「改日期／時間／時區」，用月曆同時鐘選擇；揀好即會重新計算。", "Tap Qimen → Calculate for now. No birth date, GPS, account or AI is needed. To use another time, open Change date / time / zone and use the calendar and clock; the chart updates automatically."))
            Text(t("先睇「本盤簡讀」，再點九宮查看門、星、神及天地盤干。南在上、北在下；深色邊框的事情宮，是本次簡讀的依據。盤面會保留所選時刻，唔會睇緊突然跳盤。", "Read Simple reading, then tap any palace for its symbols. South is at the top. The outlined matter palace supplies the introductory reading. The selected chart stays fixed until you change the time."))
            Text(t("按「儲存此盤」後，可在「已存盤與歷史」重看。門名如「死門」「傷門」只是傳統名稱，不代表必然發生壞事；目前簡讀只看八門，未做全盤專業占斷。", "Save a chart and reopen it under Saved charts and history. Names such as Death Door and Harm Door are traditional symbols, not predictions of harm. This basic reading uses the door, not a full professional judgment."))
        }
        CardBlock(t("3 · 寫一個具體目標", "3 · Write a concrete goal")) {
            Text(t("「目標」→「新增目標」。填寫名稱及完成標準，例如「今晚讀一頁書」，完成標準是「寫低一句重點」。限期可不填，其餘先用預設值。", "Go to Goals → New goal. Enter a title and success criteria, for example 'Read one page tonight' and 'Write one takeaway'. The deadline is optional; start with the other defaults."))
        }
        CardBlock(t("4 · 揀一個方案去做", "4 · Choose an action")) {
            Text(t("儲存目標後，按「規劃下一步」→「產生三個方案」。三個方案是不同做法，揀一個便可，毋須全部做。它們共用同一個可用時段，並非三個吉時。", "Save your goal, tap Plan next step → Generate three options. Pick one alternative; you do not need to do all three. They share a practical time window, not auspicious time slots."))
            Text(t("建議會遵守每次可用分鐘、限期與支出上限；其他文字限制仍需你自行判斷。限期已過時，先編輯目標再產生方案。", "Suggestions respect duration, deadline and spending limits. Review other written constraints yourself. If a deadline has passed, edit the goal before generating options."))
        }
        CardBlock(t("5 · 記錄，再調整", "5 · Record and adjust")) {
            Text(t("做完後，在已選方案按「記錄結果」，揀完成／部分完成／未完成／未知，寫低觀察並儲存。想改計劃，就返回「規劃下一步」，填寫新觀察再按「重新規劃，保留歷史」。", "After acting, tap Record outcome on the selected option. Choose an outcome, add notes and save. To adjust, return to Plan next step, enter an observation and tap Replan and preserve history."))
            Text(t("日誌不會自動將整個目標標成完成；達成目標後，可到「編輯」將狀態改為「已完成」。", "A journal entry does not complete the whole goal automatically. When you reach it, edit its status to Completed."))
        }
        ActionButton(t("開始建立目標", "Start with a goal"), onClick = onGoals)
        CardBlock(t("備份與私隱", "Backup and privacy")) {
            Text(t("資料預設留在手機。「設定」可匯出備份；匯入會取代現有記錄。可選 AI 需要你自行設定供應商，並且每次傳送前都要確認內容。研究資料不會自動上傳。", "Data stays on your phone by default. Export backups in Settings; importing replaces current records. Optional AI needs a configured provider and your review before each request. Research data is never uploaded automatically."))
        }
    }
}
