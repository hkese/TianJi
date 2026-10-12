package com.tianji.app
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
class JourneyTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 private fun capture(name:String){compose.waitForIdle();val shot=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot();java.io.File(compose.activity.getExternalFilesDir(null),name).outputStream().use{shot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}}
 private fun awaitText(text:String){compose.waitUntil(120000){compose.onAllNodesWithText(text,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}}
 @Test fun offlineJourney(){
  awaitText("開始離線使用");compose.onNodeWithText("開始離線使用").performScrollTo().performClick();awaitText("建立第一個目標");capture("home.png");compose.onNodeWithText("建立第一個目標").performClick();compose.onNodeWithText("新增目標").performClick();compose.onNodeWithText("目標名稱").performTextInput("Read one page");compose.onNodeWithText("怎樣算完成？").performTextInput("One page read");compose.onNodeWithText("儲存目標").performScrollTo().performClick();awaitText("Read one page");compose.onNodeWithText("規劃下一步").performScrollTo().performClick();compose.onNodeWithText("產生三個方案").performScrollTo().performClick();awaitText("釐清完成標準");capture("planner.png");compose.onAllNodesWithText("選擇此方案")[0].performScrollTo().performClick();awaitText("記錄結果");compose.onNodeWithText("記錄結果").performScrollTo().performClick();compose.onNodeWithText("完成",substring=false).performClick();compose.onNodeWithText("儲存日誌").performScrollTo().performClick();compose.waitUntil(120000){compose.onAllNodesWithText("SUCCESS",substring=true).fetchSemanticsNodes().isNotEmpty()};compose.activityRule.scenario.recreate();awaitText("行動日誌");compose.onNodeWithText("SUCCESS",substring=true).assertExists();
  compose.onNodeWithTag("tab-goals").performClick();awaitText("新增目標");awaitText("規劃下一步");compose.onNodeWithText("規劃下一步").performScrollTo().performClick();awaitText("釐清完成標準");compose.onNodeWithText("新觀察／改計劃原因").performScrollTo().performTextInput("Updated after observation");compose.onNodeWithText("重新規劃，保留歷史").performScrollTo().performClick();awaitText("Updated after observation");
  compose.onNodeWithTag("tab-settings").performClick();awaitText("顯示與時間");compose.onAllNodes(isToggleable())[0].performClick();awaitText("Display and time");compose.onNodeWithText("dark").performScrollTo().performClick();compose.activityRule.scenario.recreate();awaitText("Display and time");compose.onNodeWithText("dark").assertIsSelected();
  val shot=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot();java.io.File(compose.activity.getExternalFilesDir(null),"settings-dark.png").outputStream().use{shot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};

 }
}
