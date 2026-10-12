package com.tianji.chart
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tianji.qimen.*
import com.tianji.ui.*
import java.time.Instant
@Composable fun ChartScreen(zone:String){
 val core=remember{QimenCore()};val chart=remember(zone){core.calculateChart(Instant.now(),zone)};var palace by remember{mutableStateOf<Int?>(null)}
 Page(t("奇門圖","Qimen chart"),"BLOCKED · "+chart.ruleset.version){
  Text(t("此為固定洛書宮位圖例，並非已計算命盤。缺少權威規則及獨立參考盤，所有符號保持空白。","This is a fixed Lo Shu position legend, not a calculated chart. Authoritative rules and independent fixtures are missing; all symbols remain empty."))
  listOf(listOf(4,9,2),listOf(3,5,7),listOf(8,1,6)).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){row.forEach{i->OutlinedButton(onClick={palace=i},modifier=Modifier.weight(1f).heightIn(min=88.dp)){Text(t("$i 宮\n未計算","Palace $i\nNot calculated"))}}}}
  CardBlock(t("推導記錄","Derivation trace")){chart.derivation_steps.forEach{Text(it)};Text("0 / 100 independent golden fixtures");Text("SHA-256: "+chart.input_hash,style=MaterialTheme.typography.bodySmall)}
  CardBlock(t("7 / 30 天日曆","7 / 30 day calendar")){Text(t("需要真實 Core 排盤，現時未能顯示吉凶或機會評級。","Requires actual verified Core charts. Opportunity rankings are unavailable."))}
  Text(t("傳統解讀及時空導航假說未經科學證實。","Traditional interpretations and the navigation hypothesis are not scientifically established."))
 }
 palace?.let{i->AlertDialog(onDismissRequest={palace=null},title={Text(t("第 $i 宮","Palace $i"))},text={Text(t("固定位置圖例。門、星、神、天地盤干均未計算；中央寄宮規則亦未核實。","Fixed position legend. Doors, stars, deities and stems are not calculated. Center handling is not verified."))},confirmButton={TextButton(onClick={palace=null}){Text(t("明白","Close"))}})}
}
