package com.tianji.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable fun goalStatus(value: String): String = when (value) {
    "ACTIVE" -> t("進行中", "Active")
    "PAUSED" -> t("已暫停", "Paused")
    "DONE" -> t("已完成", "Completed")
    else -> t("未知", "Unknown")
}

@Composable fun outcomeLabel(value: String): String = when (value) {
    "SUCCESS" -> t("完成", "Completed")
    "PARTIAL" -> t("部分完成", "Partly completed")
    "NOT_YET" -> t("未完成", "Not yet")
    else -> t("未知", "Unknown")
}

@Composable fun actionTitle(kind: String, fallback: String): String = if (!English.current) fallback else when (kind) {
    "CLARIFY" -> "Clarify the completion criteria"
    "SMALL_STEP" -> "Take the smallest useful step"
    "REVIEW" -> "Review friction and the next step"
    else -> fallback
}

@Composable fun LanguageSelector(english: Boolean, onChange: (Boolean) -> Unit) {
    Text(t("介面語言", "App language"), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(!english, { onChange(false) }, modifier = Modifier.testTag("language-zh"), label = { Text("繁體中文") })
        FilterChip(english, { onChange(true) }, modifier = Modifier.testTag("language-en"), label = { Text("English") })
    }
}
