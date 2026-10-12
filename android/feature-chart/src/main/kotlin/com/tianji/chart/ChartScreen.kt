package com.tianji.chart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tianji.domain.ChartSnapshot
import com.tianji.qimen.*
import com.tianji.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.DateTimeFormatter

@Composable fun ChartScreen(zone: String, saved: List<ChartSnapshot>, onZone: (String) -> Unit,
    onSave: (Chart) -> Unit, onGoals: () -> Unit, onGuide: () -> Unit) {
    val core = remember { QimenCore() }
    var instantText by rememberSaveable { mutableStateOf(Instant.now().toString()) }
    var controls by rememberSaveable { mutableStateOf(false) }
    var datePicker by rememberSaveable { mutableStateOf(false) }
    var timePicker by rememberSaveable { mutableStateOf(false) }
    var details by rememberSaveable { mutableStateOf(false) }
    var history by rememberSaveable { mutableStateOf(false) }
    var palace by rememberSaveable { mutableStateOf<Int?>(null) }
    var ambiguous by remember { mutableStateOf<List<Instant>>(emptyList()) }
    var invalidTime by rememberSaveable { mutableStateOf(false) }
    val local = remember(instantText, zone) { Instant.parse(instantText).atZone(ZoneId.of(zone)) }
    fun choose(date: LocalDate, time: LocalTime) {
        val candidates = ChartTime.candidates(date, time.withSecond(0).withNano(0), ZoneId.of(zone))
        invalidTime = candidates.isEmpty()
        when (candidates.size) { 1 -> instantText = candidates.single().toString(); 2 -> ambiguous = candidates }
    }
    val result by produceState<Result<Chart>?>(null, instantText, zone) {
        value = null
        value = withContext(Dispatchers.Default) { runCatching { core.calculateChart(Instant.parse(instantText), zone) } }
    }
    val chart = result?.getOrNull()
    val english = English.current
    Page(t("離線奇門", "Offline Qimen"), t("起一個盤，睇當下嘅傳統提示。", "A chart and a simple traditional reading.")) {
        CardBlock(t("起盤時間", "Chart time")) {
            Text(local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm")), style = MaterialTheme.typography.headlineSmall)
            Text(zoneCity(zone, english) + " · UTC" + local.offset.toString().replace("Z", "+00:00"), style = MaterialTheme.typography.bodyMedium)
            ActionButton(t("用現在時間起盤", "Calculate for now")) { instantText = Instant.now().toString(); invalidTime = false }
            TextButton(onClick = { controls = !controls }, modifier = Modifier.testTag("chart-time-settings")) {
                Text(t("改日期／時間／時區", "Change date / time / zone"))
            }
            if (controls) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { datePicker = true }, modifier = Modifier.weight(1f).testTag("chart-date")) { Text(t("選日期", "Pick date")) }
                    OutlinedButton(onClick = { timePicker = true }, modifier = Modifier.weight(1f).testTag("chart-time")) { Text(t("選時間", "Pick time")) }
                }
                TimeZoneSelector(zone, Instant.parse(instantText)) { invalidTime = false; onZone(it) }
                Text(t("按所選城市的鐘錶時間排盤，23:00 換日；不作真太陽時校正。", "Uses the selected city's civil clock with a 23:00 day boundary; no apparent solar-time correction."), style = MaterialTheme.typography.bodySmall)
            }
            if (invalidTime) ErrorText(t("這個時間因夏令時間跳時而不存在，請選另一個時間。", "This time does not exist during the daylight-saving jump. Choose another time."))
        }
        if (result == null) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(t("正在手機起盤…", "Calculating on your phone…")) }
        else if (chart == null) CardBlock(t("未能起盤", "Unable to calculate")) {
            Text(t("請選擇 1900 至 2100 年的日期及有效時區，再按「用現在時間起盤」重試。", "Choose a date from 1900 to 2100 and a valid time zone, or calculate for now."))
        }
        if (chart != null) {
            val reading = remember(chart) { TraditionalReading.from(chart) }
            val c = chart.calendar
            CardBlock(t("本盤簡讀", "Simple reading")) {
                Text(t("傳統推演 · 非科學預測", "TRADITIONAL · Not a scientific prediction"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(if (english) reading.themeEn else reading.themeZh, style = MaterialTheme.typography.headlineMedium)
                Text(if (english) reading.meaningEn else reading.meaningZh)
                HorizontalDivider()
                Text(t("可以試的一小步", "One step to consider"), style = MaterialTheme.typography.titleSmall)
                Text(if (english) reading.stepEn else reading.stepZh)
                Text(t("依據：${c["hour_ganzhi"]}時，時干${reading.stem}在天盤${reading.palace.index}宮（${reading.palace.direction}），遇${reading.palace.door}。", "Based on ${c["hour_ganzhi"]}: hour stem ${reading.stem} in heaven plate palace ${reading.palace.index}, with ${reading.palace.door}."), style = MaterialTheme.typography.bodySmall)
                Text(t("這是只看事情宮八門的入門解讀，未綜合全盤格局，不能保證事件結果。來源：《煙波釣叟歌》八門象意；現代小步建議由本 App 整理。", "An introductory reading of the matter palace's door, not a full chart judgment or guaranteed outcome. Door symbolism: Yanbo Diaosou Ge; practical steps: TianJi editorial guidance."), style = MaterialTheme.typography.bodySmall)
            }
            Text("${chart.dun_type}${chart.ju_number}局 · ${c["solar_term"]}${c["yuan"]}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("chart-summary"))
            Text(t("${c["year_ganzhi"]}年 ${c["month_ganzhi"]}月 ${c["day_ganzhi"]}日 ${c["hour_ganzhi"]}時", "Year ${c["year_ganzhi"]} · Month ${c["month_ganzhi"]} · Day ${c["day_ganzhi"]} · Hour ${c["hour_ganzhi"]}"))
            Text(t("值符 ${c["duty_star"]} · 值使 ${c["duty_door"]}", "Duty star ${c["duty_star"]} · Duty door ${c["duty_door"]}"))
            Text(t("南方在上；點宮位睇詳情。深色邊框是本次解讀用的事情宮。", "South is at the top. Tap a palace for details; the outlined palace is used for the reading."), style = MaterialTheme.typography.bodySmall)
            listOf(listOf(4, 9, 2), listOf(3, 5, 7), listOf(8, 1, 6)).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { i ->
                        val p = chart.palaces[i-1]
                        val description = t("${p.direction}，${i}宮，門${p.door ?: "無"}，星${p.star ?: "寄宮"}，神${p.deity ?: "無"}，天盤${p.heaven_stem ?: "無"}，地盤${p.earth_stem}", "Palace $i: door ${p.door}, star ${p.star}, deity ${p.deity}, heaven ${p.heaven_stem}, earth ${p.earth_stem}")
                        OutlinedCard(onClick = { palace = i }, modifier = Modifier.weight(1f).heightIn(min = 164.dp).testTag("palace-$i").semantics(mergeDescendants = true) { contentDescription = description },
                            border = BorderStroke(if(i == reading.palace.index) 2.dp else 1.dp, if(i == reading.palace.index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(t("${i}宮 · ${p.direction}", "Palace $i"), style = MaterialTheme.typography.labelSmall)
                                Text(p.door ?: t("中宮", "Center"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                if(i == 5) {
                                    Text(t("天禽隨芮\n寄${c["center_heaven_host"]}宮", "Tianqin with Tianrui\nin ${c["center_heaven_host"]}"), style = MaterialTheme.typography.bodySmall)
                                } else {
                                    Text(p.star!!.replace("、天禽", "\n天禽寄"), style = MaterialTheme.typography.bodySmall)
                                    Text(p.deity!!, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(t("天 ${p.heaven_stem ?: "—"}\n地 ${p.earth_stem}", "H ${p.heaven_stem ?: "—"}\nE ${p.earth_stem}"), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            if (chart.warnings.contains("NEAR_SOLAR_TERM_BOUNDARY")) Text(t("接近交節兩分鐘內：不同曆表可能有秒數差異，請留意局數分界。", "Within two minutes of a solar term: ephemeris differences may affect the boundary."), color = MaterialTheme.colorScheme.error)
            val isSaved = saved.any { it.id == chart.chart_id }
            ActionButton(if(isSaved) t("已儲存此盤", "Chart saved") else t("儲存此盤，方便之後重看", "Save chart for later"), enabled = !isSaved) { onSave(chart) }
            TextButton(onClick = { details = !details }, modifier = Modifier.testTag("chart-details")) { Text(t("排盤步驟與驗證資料", "Derivation and checks")) }
            if (details) CardBlock(t("怎樣得出這個盤？", "How was this calculated?")) {
                Text(t("時家轉盤拆補 · 子初換日 · 中五寄坤二 · 天禽隨芮", "Shi Jia rotating Chai Bu · 23:00 boundary · center hosted in Kun 2 · Tianqin follows Tianrui"))
                chart.derivation_steps.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(t("已核對：1 個公開逐宮例盤、351 個跨程式比較盤、12 個天文台中氣時刻。跨程式比對不等於人工金標；100 個獨立人工金標目標未完成。", "Checked: 1 published worked chart, 351 cross-implementation charts and 12 HKO solar terms. Cross-checks are not human goldens; the 100 independent human-golden target remains incomplete."))
                Text(t("規則版本：", "Ruleset: ") + chart.ruleset.version, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(onClick = { history = !history }, modifier = Modifier.fillMaxWidth().testTag("chart-history")) { Text(t("已存盤與歷史", "Saved charts and history")) }
        if (history) {
            val recent = saved.filter { it.rulesetId == ShiJiaChaiBu.metadata.id }.sortedByDescending { it.instantUtc }.take(30)
            if(recent.isEmpty()) Text(t("未有已存盤；起盤後按「儲存此盤」。", "No saved charts yet. Calculate one and tap Save chart."))
            recent.forEach { snapshot ->
                OutlinedButton(onClick = { instantText = snapshot.instantUtc; if(zone != snapshot.timezone) onZone(snapshot.timezone); history = false }, modifier = Modifier.fillMaxWidth()) {
                    Text(displayTime(snapshot.instantUtc, snapshot.timezone) + " · " + zoneCity(snapshot.timezone, english))
                }
            }
            Text(t("顯示最新 30 個時刻；全部已存盤都包括在設定頁的備份內。", "Showing the 30 latest chart times. Backups in Settings include all saved charts."), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick = onGuide, modifier = Modifier.fillMaxWidth()) { Text(t("使用教學", "Getting started")) }
        OutlinedButton(onClick = onGoals, modifier = Modifier.fillMaxWidth()) { Text(t("去規劃今日行動", "Plan a practical action")) }
    }
    if(datePicker) ChartDateDialog(local.toLocalDate(), { datePicker = false }) { choose(it, local.toLocalTime()); datePicker = false }
    if(timePicker) ChartClockDialog(local.toLocalTime(), { timePicker = false }) { choose(local.toLocalDate(), it); timePicker = false }
    if(ambiguous.isNotEmpty()) AlertDialog(onDismissRequest = { ambiguous = emptyList() }, title = { Text(t("這個鐘面時間出現兩次", "This clock time occurs twice")) },
        text = { Column { Text(t("夏令時間回撥，請選擇所指的一次。", "The clock moved back. Choose which occurrence you mean.")); ambiguous.forEachIndexed { i, instant ->
            TextButton(onClick = { instantText = instant.toString(); ambiguous = emptyList() }) { Text(t("第 ${i+1} 次", "Occurrence ${i+1}") + " · UTC" + instant.atZone(ZoneId.of(zone)).offset) }
        } } }, confirmButton = {}, dismissButton = { TextButton(onClick = { ambiguous = emptyList() }) { Text(t("取消", "Cancel")) } })
    palace?.let { index -> chart?.palaces?.get(index-1)?.let { p ->
        AlertDialog(onDismissRequest = { palace = null }, title = { Text(t("${p.direction} · 第 $index 宮", "Palace $index")) },
            text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(t("門：${p.door ?: "中宮不排門"}\n星：${p.star ?: "天禽隨天芮寄宮"}\n神：${p.deity ?: "中宮不排神"}\n天盤干：${p.heaven_stem ?: "隨天禽寄宮"}\n地盤干：${p.earth_stem}", "Door: ${p.door ?: "none in center"}\nStar: ${p.star ?: "hosted with Tianrui"}\nDeity: ${p.deity ?: "none in center"}\nHeaven stems: ${p.heaven_stem ?: "hosted"}\nEarth stems: ${p.earth_stem}"))
                Text(t("有兩個干時，前者是本干，後者是中五寄干。星、神是傳統符號名稱，不代表真實神靈或已證實的力量。", "When two stems appear, the second is the hosted center stem. Stars and deities are traditional symbolic names, not evidence of supernatural forces."), style = MaterialTheme.typography.bodySmall)
            } }, confirmButton = { TextButton(onClick = { palace = null }) { Text(t("返回盤面", "Back to chart")) } })
    } }
}
