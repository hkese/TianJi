package com.tianji.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JourneyTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        compose.waitForIdle()
        val shot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        java.io.File(compose.activity.getExternalFilesDir(null), name).outputStream().use {
            shot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        // Test-only shared output survives Gradle's post-test app uninstall.
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val resolver = compose.activity.contentResolver
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, name)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TianJiTests")
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = requireNotNull(resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            requireNotNull(resolver.openOutputStream(uri)).use {
                shot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            values.clear()
            values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(120000) {
            compose.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun awaitKeyboard() {
        compose.waitUntil(120000) {
            compose.runOnIdle {
                ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            }
        }
    }

    private fun assertAboveKeyboard(text: String) {
        val bounds = compose.onNodeWithText(text).fetchSemanticsNode().boundsInWindow
        val keyboardTop = compose.runOnIdle {
            val view = compose.activity.window.decorView
            view.height - (ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0)
        }
        assertTrue("$text must remain above the real keyboard", bounds.bottom <= keyboardTop + 1)
    }

    @Test fun offlineJourney() {
        awaitText("開始離線使用")
        compose.onNodeWithTag("language-zh").assertIsSelected()
        compose.onNodeWithTag("timezone-selector").performScrollTo().performClick()
        compose.onNodeWithTag("timezone-device").assertExists()
        compose.onNodeWithTag("timezone-search").performClick().performTextInput("Seattle")
        compose.onNodeWithTag("timezone-America/Los_Angeles").assertIsDisplayed()
        capture("timezone-search.png")
        compose.onNodeWithTag("timezone-America/Los_Angeles").performClick()
        compose.onNodeWithText("西雅圖／洛杉磯（美國太平洋時間）").assertExists()
        capture("onboarding.png")
        compose.onNodeWithText("開始離線使用").performScrollTo().performClick()
        awaitText("建立第一個目標")
        capture("home.png")
        compose.onNodeWithText("使用教學").performScrollTo().performClick()
        awaitText("目前可以做甚麼？")
        capture("guide.png")
        compose.onNodeWithText("返回", substring = false).performScrollTo().performClick()
        awaitText("建立第一個目標")
        compose.onNodeWithText("建立第一個目標").performScrollTo().performClick()
        compose.onNodeWithText("新增目標").performClick()
        compose.onNodeWithText("已完成").performScrollTo().assertExists()
        compose.onNodeWithText("ACTIVE").assertDoesNotExist()
        compose.onNodeWithTag("deadline-picker").performScrollTo().performClick()
        compose.onNodeWithText("選擇限期").assertExists()
        compose.onNodeWithText("Selected date").assertDoesNotExist()
        assertTrue("Calendar month must be Chinese even when Android is English",
            compose.onAllNodesWithText("月", substring = true).fetchSemanticsNodes().isNotEmpty())
        capture("date-picker-zh.png")
        compose.onNode(hasText("取消") and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText("學習／練習").performScrollTo().performClick()
        compose.onNodeWithText("目標名稱").performScrollTo().performClick().performTextInput("Read one page")
        awaitKeyboard()
        compose.onNodeWithText("怎樣算完成？").performTextInput("One page read")
        compose.onNodeWithText("儲存目標").performScrollTo().assertIsDisplayed()
        assertAboveKeyboard("儲存目標")
        capture("goal-keyboard.png")
        compose.onNodeWithText("儲存目標").performClick()
        awaitText("Read one page")
        compose.onNodeWithText("規劃下一步").performScrollTo().performClick()
        compose.onNodeWithText("產生三個方案").performScrollTo().performClick()
        awaitText("1 · 讀一小段，寫一句重點")
        compose.onAllNodesWithText("選擇此方案").assertCountEquals(3)
        compose.onAllNodesWithText("可選象意方位：", substring=true).assertCountEquals(3)
        compose.onNodeWithText("交畀 ChatGPT／Codex 分析").performScrollTo().performClick()
        compose.onNodeWithText("帶住呢個盤問 AI").assertExists()
        compose.onNodeWithTag("handoff-preview").performClick()
        compose.onNodeWithTag("handoff-prompt").assertExists()
        compose.onNodeWithTag("handoff-preview").performScrollTo().performClick()
        compose.onNodeWithText("複製完整提示詞").performScrollTo().performClick()
        compose.onNodeWithText("已複製 · 下一步開 ChatGPT").assertExists()
        compose.runOnIdle {
            val clipboard=compose.activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val prompt=clipboard.primaryClip!!.getItemAt(0).text.toString()
            assertTrue(prompt.contains("Read one page"))
            assertTrue(prompt.contains("<context_json>"))
            assertTrue(prompt.contains("shijia-zhuanpan-chaibu-civil-kun2"))
        }
        capture("handoff-zh.png")
        compose.onNodeWithText("返回天機方案").performScrollTo().performClick()
        compose.onNodeWithText("1 · 讀一小段，寫一句重點").performScrollTo()
        capture("navigation-zh.png")
        compose.onAllNodesWithText("選擇此方案")[0].performScrollTo().performClick()
        awaitText("記錄結果")
        compose.onNodeWithText("你已揀好 · 即刻做呢一步").performScrollTo().assertIsDisplayed()
        capture("selected-next-step.png")
        compose.onNodeWithText("記錄結果").performScrollTo().performClick()
        compose.onNodeWithText("完成", substring = false).performClick()
        compose.onNodeWithText("儲存日誌").performScrollTo().performClick()
        compose.waitUntil(120000) {
            compose.onAllNodesWithText(" · 完成", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.activityRule.scenario.recreate()
        awaitText("行動日誌")
        compose.onNodeWithText(" · 完成", substring = true).assertExists()
        compose.onNodeWithText("SUCCESS", substring = true).assertDoesNotExist()

        compose.onNodeWithTag("tab-goals").performClick()
        awaitText("新增目標")
        awaitText("規劃下一步")
        compose.onNodeWithText("規劃下一步").performScrollTo().performClick()
        awaitText("1 · 讀一小段，寫一句重點")
        compose.onNodeWithText("新觀察／改計劃原因").performScrollTo()
            .performTextInput("Updated after observation")
        compose.onNodeWithText("重新規劃，保留歷史").performScrollTo().performClick()
        awaitText("Updated after observation")
        // Dismiss the IME first, as on a phone, then navigate back.
        val keyboardVisible = compose.runOnIdle {
            ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        if (keyboardVisible) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("input keyevent KEYCODE_BACK").close()
            compose.waitUntil(120000) {
                compose.runOnIdle {
                    ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                        ?.isVisible(WindowInsetsCompat.Type.ime()) != true
                }
            }
        }
        // Exercise Android's system back dispatch after a replan.
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("input keyevent KEYCODE_BACK").close()
        awaitText("新增目標")

        compose.onNodeWithTag("tab-settings").performClick()
        awaitText("顯示與時間")
        compose.onNodeWithText("西雅圖／洛杉磯（美國太平洋時間）").assertExists()
        compose.onNodeWithTag("timezone-selector").performScrollTo().performClick()
        compose.onNodeWithTag("timezone-Asia/Hong_Kong").performClick()
        compose.activityRule.scenario.recreate()
        awaitText("顯示與時間")
        compose.onNodeWithText("香港", substring = false).assertExists()
        // Dismissing a search must not overwrite the stored zone.
        compose.onNodeWithTag("timezone-selector").performScrollTo().performClick()
        compose.onNodeWithTag("timezone-search").performTextInput("London")
        compose.onNodeWithTag("timezone-Europe/London").assertIsDisplayed()
        compose.onNodeWithTag("timezone-cancel").performClick()
        compose.onNodeWithText("香港", substring = false).assertExists()
        compose.onNodeWithTag("language-en").performScrollTo().performClick()
        awaitText("Display and time")
        compose.onNodeWithText("Hong Kong", substring = false).assertExists()
        compose.onNodeWithTag("theme-dark").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        awaitText("Display and time")
        compose.onNodeWithTag("theme-dark").assertIsSelected()
        compose.onNodeWithTag("language-zh").performScrollTo().performClick()
        awaitText("顯示與時間")
        compose.onNodeWithText("深色").assertIsSelected()
        capture("settings-dark.png")
        compose.onNodeWithTag("tab-chart").performClick()
        awaitText("本盤簡讀")
        compose.onNodeWithText("未計算", substring = true).assertDoesNotExist()
        compose.onNodeWithTag("chart-time-settings").performScrollTo().performClick()
        compose.onNodeWithTag("chart-date").performScrollTo().performClick()
        compose.onNodeWithText("選擇起盤日期").assertExists()
        compose.onNodeWithText("Selected date").assertDoesNotExist()
        capture("chart-date-picker.png")
        compose.onNodeWithTag("chart-date-confirm").performClick()
        awaitText("本盤簡讀")
        compose.onNodeWithTag("chart-time").performScrollTo().performClick()
        compose.onNodeWithText("選擇起盤時間").assertExists()
        capture("chart-time-picker.png")
        compose.onNodeWithTag("chart-time-confirm").performClick()
        awaitText("本盤簡讀")
        compose.onNodeWithTag("chart-time-settings").performScrollTo().performClick()
        compose.onNodeWithText("本盤簡讀").performScrollTo()
        capture("chart-reading.png")
        compose.onNodeWithTag("palace-4").performScrollTo().performClick()
        compose.onNodeWithText("門：", substring = true).assertExists()
        capture("chart-palace-detail.png")
        compose.onNodeWithText("返回盤面").performClick()
        compose.onNodeWithTag("palace-8").performScrollTo()
        capture("chart-grid.png")
        compose.onNodeWithText("儲存此盤，方便之後重看").performScrollTo().performClick()
        awaitText("已儲存此盤")
        compose.activityRule.scenario.recreate()
        awaitText("本盤簡讀")
        compose.onNodeWithText("已儲存此盤").assertExists()
        compose.onNodeWithTag("chart-history").performScrollTo().performClick()
        compose.onNodeWithText("未有已存盤；起盤後按「儲存此盤」。").assertDoesNotExist()
        compose.onNodeWithTag("chart-details").performScrollTo().performClick()
        compose.onNodeWithText("已核對：1 個公開逐宮例盤", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("去規劃今日行動").performScrollTo().performClick()
        awaitText("新增目標")
    }
}
