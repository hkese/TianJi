package com.tianji.ui

import android.icu.text.TimeZoneNames
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tianji.domain.CityZone
import com.tianji.domain.TimeZones
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

fun zoneCity(id: String, english: Boolean): String {
    TimeZones.common.firstOrNull { it.id == id }?.let { return if (english) it.english else it.chinese }
    val locale = if (english) Locale.ENGLISH else Locale.forLanguageTag("zh-HK")
    return TimeZoneNames.getInstance(locale).getExemplarLocationName(id)
        ?: id.substringAfterLast('/').replace('_', ' ')
}

@Composable
fun TimeZoneSelector(zone: String, referenceInstant: Instant = Instant.now(), onSelect: (String) -> Unit) {
    var opened by rememberSaveable { mutableStateOf(false) }
    val english = English.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(t("時區", "Time zone"), style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { opened = true }, modifier = Modifier.fillMaxWidth().testTag("timezone-selector")) {
            Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                Text(zoneCity(zone, english))
                Text(TimeZones.offset(zone, referenceInstant), style = MaterialTheme.typography.bodySmall)
            }
            Text(t("更改", "Change"))
        }
        Text(t("按城市選擇，夏令時間會自動換算，毋須輸入時區代碼。", "Choose a city; daylight saving is handled automatically. No time-zone codes needed."), style = MaterialTheme.typography.bodySmall)
    }
    if (opened) TimeZoneDialog(zone, onDismiss = { opened = false }) { id ->
        onSelect(id)
        opened = false
    }
}

@Composable
private fun TimeZoneDialog(zone: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val english = English.current
    var query by rememberSaveable { mutableStateOf("") }
    val now = remember { Instant.now() }
    val device = remember { TimeZones.deviceZone() }
    val cities = remember(english) {
        val presets = TimeZones.common.map { it.id }.toSet()
        TimeZones.common + ZoneId.getAvailableZoneIds().filter { it !in presets }.map {
            CityZone(it, zoneCity(it, false), zoneCity(it, true))
        }.sortedBy { if (english) it.english else it.chinese }
    }
    val results = remember(query, cities) { cities.filter { TimeZones.matches(it, query) } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.safeDrawingPadding().padding(16.dp).widthIn(max = 560.dp)
            .fillMaxWidth().fillMaxHeight(0.94f), shape = MaterialTheme.shapes.extraLarge) {
            Column(Modifier.imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(t("選擇時區", "Choose a time zone"), style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().testTag("timezone-search"),
                    label = { Text(t("搜尋城市或地區", "Search city or region")) }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) })
                if (query.isBlank()) {
                    OutlinedButton(onClick = { onSelect(device) }, modifier = Modifier.fillMaxWidth().testTag("timezone-device")) {
                        Column {
                            Text(t("使用手機目前時區", "Use device's current time zone"))
                            Text(zoneCity(device, english), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Text(if (query.isBlank()) t("常用城市及所有時區", "Popular cities and all time zones") else t("搜尋結果", "Search results"), style = MaterialTheme.typography.labelLarge)
                LazyColumn(Modifier.weight(1f).testTag("timezone-results")) {
                    if (results.isEmpty()) item { Text(t("搵唔到？試試附近大城市，或清空搜尋後選用手機時區。", "No match. Try a nearby major city, or clear the search to use your device's zone.")) }
                    items(results, key = { it.id }) { city ->
                        Row(Modifier.fillMaxWidth().testTag("timezone-${city.id}")
                            .semantics { selected = city.id == zone }
                            .clickable(role = Role.RadioButton) { onSelect(city.id) }
                            .padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(if (english) city.english else city.chinese, style = MaterialTheme.typography.bodyLarge)
                                Text(TimeZones.offset(city.id, now), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (city.id == zone) Icon(Icons.Default.Check, contentDescription = t("已選擇", "Selected"))
                        }
                        HorizontalDivider()
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).testTag("timezone-cancel")) { Text(t("取消", "Cancel")) }
            }
        }
    }
}
