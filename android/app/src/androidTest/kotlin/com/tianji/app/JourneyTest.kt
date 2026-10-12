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
        capture("onboarding.png")
        compose.onNodeWithText("開始離線使用").performScrollTo().performClick()
        awaitText("建立第一個目標")
        capture("home.png")
        compose.onNodeWithText("建立第一個目標").performClick()
        compose.onNodeWithText("新增目標").performClick()
        compose.onNodeWithText("目標名稱").performClick().performTextInput("Read one page")
        awaitKeyboard()
        compose.onNodeWithText("怎樣算完成？").performTextInput("One page read")
        compose.onNodeWithText("儲存目標").performScrollTo().assertIsDisplayed()
        assertAboveKeyboard("儲存目標")
        capture("goal-keyboard.png")
        compose.onNodeWithText("儲存目標").performClick()
        awaitText("Read one page")
        compose.onNodeWithText("規劃下一步").performScrollTo().performClick()
        compose.onNodeWithText("產生三個方案").performScrollTo().performClick()
        awaitText("釐清完成標準")
        compose.onAllNodesWithText("選擇此方案").assertCountEquals(3)
        capture("planner.png")
        compose.onAllNodesWithText("選擇此方案")[0].performScrollTo().performClick()
        awaitText("記錄結果")
        compose.onNodeWithText("記錄結果").performScrollTo().performClick()
        compose.onNodeWithText("完成", substring = false).performClick()
        compose.onNodeWithText("儲存日誌").performScrollTo().performClick()
        compose.waitUntil(120000) {
            compose.onAllNodesWithText("SUCCESS", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.activityRule.scenario.recreate()
        awaitText("行動日誌")
        compose.onNodeWithText("SUCCESS", substring = true).assertExists()

        compose.onNodeWithTag("tab-goals").performClick()
        awaitText("新增目標")
        awaitText("規劃下一步")
        compose.onNodeWithText("規劃下一步").performScrollTo().performClick()
        awaitText("釐清完成標準")
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
        compose.onAllNodes(isToggleable())[0].performClick()
        awaitText("Display and time")
        compose.onNodeWithText("dark").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        awaitText("Display and time")
        compose.onNodeWithText("dark").assertIsSelected()
        capture("settings-dark.png")
    }
}
