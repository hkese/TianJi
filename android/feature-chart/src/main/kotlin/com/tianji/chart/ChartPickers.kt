package com.tianji.chart

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tianji.ui.English
import com.tianji.ui.t
import java.time.*
import java.util.Locale

@Composable private fun PickerLocale(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val english = English.current
    val localized = remember(configuration, english) { Configuration(configuration).apply { setLocale(if(english) Locale.ENGLISH else Locale.forLanguageTag("zh-HK")) } }
    val localizedContext = remember(context, localized) { context.createConfigurationContext(localized) }
    CompositionLocalProvider(LocalContext provides localizedContext, LocalConfiguration provides localized, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun ChartDateDialog(initial: LocalDate, dismiss: () -> Unit, choose: (LocalDate) -> Unit) {
    PickerLocale {
        val date = initial.coerceIn(LocalDate.of(1900,1,1), LocalDate.of(2100,12,31))
        val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), yearRange = 1900..2100)
        DatePickerDialog(onDismissRequest = dismiss, confirmButton = {
            TextButton(enabled = state.selectedDateMillis != null, onClick = { state.selectedDateMillis?.let { choose(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } }, modifier = Modifier.testTag("chart-date-confirm")) { Text(t("確定日期", "Confirm date")) }
        }, dismissButton = { TextButton(onClick = dismiss) { Text(t("取消", "Cancel")) } }) {
            PickerLocale { DatePicker(state, showModeToggle = false, title = { Text(t("選擇起盤日期", "Choose chart date"), Modifier.padding(start=24.dp,top=16.dp)) }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun ChartClockDialog(initial: LocalTime, dismiss: () -> Unit, choose: (LocalTime) -> Unit) {
    PickerLocale {
        val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
        Dialog(onDismissRequest = dismiss) {
            PickerLocale {
                Surface(shape = MaterialTheme.shapes.extraLarge) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(t("選擇起盤時間", "Choose chart time"), style = MaterialTheme.typography.titleLarge)
                        Text(t("24 小時制；先點小時，再點分鐘。", "24-hour clock. Tap the hour, then minutes."), style = MaterialTheme.typography.bodySmall)
                        TimePicker(state)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = dismiss) { Text(t("取消", "Cancel")) }
                            TextButton(onClick = { choose(LocalTime.of(state.hour, state.minute)) }, modifier = Modifier.testTag("chart-time-confirm")) { Text(t("確定時間", "Confirm time")) }
                        }
                    }
                }
            }
        }
    }
}
