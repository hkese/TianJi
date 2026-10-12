package com.tianji.chart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.tianji.qimen.QimenCore
import com.tianji.ui.*
import java.time.Instant

@Composable fun ChartScreen(zone: String, onGoals: () -> Unit, onGuide: () -> Unit) {
    val core = remember { QimenCore() }
    val chart = remember(zone) { core.calculateChart(Instant.now(), zone) }
    var legend by rememberSaveable { mutableStateOf(false) }
    var details by rememberSaveable { mutableStateOf(false) }
    var palace by rememberSaveable { mutableStateOf<Int?>(null) }
    Page(t("奇門", "Qimen"), t("功能狀態", "Feature status")) {
        CardBlock(t("奇門排盤尚未開放", "Qimen calculation is not available yet")) {
            Text(t("這不是載入中，也不是你設定錯誤。現時未完成排盤規則及參考命盤核對，因此不會產生門、星、神、天干或吉時。", "This is not loading, and your settings are not at fault. Rules and independent reference charts have not been verified, so doors, stars, deities, stems and auspicious times are not calculated."))
            Text(t("更改時區或連接 AI 都不會啟用排盤。", "Changing time zones or connecting AI will not enable chart calculation."))
        }
        CardBlock(t("現在可以用：離線行動規劃", "Available now: offline action planning")) {
            Text(t("先建立目標，再比較三個實際做法、記錄結果及調整計劃。這些建議依據你的目標與限制，沒有使用奇門數據。", "Create a goal, compare three practical actions, record outcomes and adjust. These suggestions use your goals and constraints, not Qimen data."))
            ActionButton(t("去規劃今日行動", "Plan an action"), onClick = onGoals)
            OutlinedButton(onClick = onGuide) { Text(t("睇使用教學", "Read the guide")) }
        }
        OutlinedButton(onClick = { legend = !legend }, modifier = Modifier.fillMaxWidth()) {
            Text(if (legend) t("收起九宮方位圖例", "Hide position legend") else t("認識九宮方位（圖例，非排盤）", "Nine-palace positions (legend only)"))
        }
        if (legend) {
            Text(t("只展示固定位置，沒有計算吉凶。南方在上，北方在下。", "Fixed positions only, with no auspiciousness calculation. South is at the top, north at the bottom."))
            val names = mapOf(4 to t("東南", "SE"), 9 to t("南", "S"), 2 to t("西南", "SW"), 3 to t("東", "E"), 5 to t("中央", "Center"), 7 to t("西", "W"), 8 to t("東北", "NE"), 1 to t("北", "N"), 6 to t("西北", "NW"))
            listOf(listOf(4, 9, 2), listOf(3, 5, 7), listOf(8, 1, 6)).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { i ->
                        OutlinedButton(onClick = { palace = i }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).heightIn(min = 88.dp)) {
                            Text(t("$i 宮", "Palace $i") + "\n" + names.getValue(i))
                        }
                    }
                }
            }
        }
        TextButton(onClick = { details = !details }, modifier = Modifier.testTag("chart-details")) {
            Text(if (details) t("收起驗證進度", "Hide verification progress") else t("查看驗證進度", "Verification progress"))
        }
        if (details) CardBlock(t("驗證進度", "Verification progress")) {
            Text(t("已核對獨立參考盤：0／100。尚未完成：節氣、干支、陰陽遁、局數及九宮符號計算。", "Independent reference charts checked: 0/100. Solar terms, stems/branches, Dun, Ju and palace symbols remain unverified."))
            Text(t("目前只處理日期時間及所選時區，沒有進行奇門推導。", "Only civil time and the selected time zone are processed; no Qimen derivation takes place."))
            Text(t("輸入時間：", "Input time: ") + displayTime(chart.input.instant_utc, zone))
            Text(t("時區：", "Time zone: ") + zoneCity(zone, English.current))
            Text(t("規則版本：", "Ruleset version: ") + chart.ruleset.version, style = MaterialTheme.typography.bodySmall)
        }
        Text(t("傳統奇門解讀及時空導航假說未經科學證實。", "Traditional interpretations and the navigation hypothesis are not scientifically established."), style = MaterialTheme.typography.bodySmall)
    }
    palace?.let { i ->
        AlertDialog(onDismissRequest = { palace = null }, title = { Text(t("第 $i 宮 · 方位圖例", "Palace $i · Position legend")) },
            text = { Text(t("這格只表示固定宮位，並非已計算命盤。門、星、神、天地盤干及中央寄宮規則仍待核實。", "This is a fixed position, not a calculated chart. Symbols and center handling remain unverified.")) },
            confirmButton = { TextButton(onClick = { palace = null }) { Text(t("明白", "Close")) } })
    }
}
