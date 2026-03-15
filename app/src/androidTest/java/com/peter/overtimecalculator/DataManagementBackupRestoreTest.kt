package com.peter.overtimecalculator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataManagementBackupRestoreTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareDevice() {
        runShellCommand("input keyevent KEYCODE_WAKEUP")
        runShellCommand("wm dismiss-keyguard")
        runShellCommand("input keyevent 82")
    }

    @Test
    fun dataManagementShowsSeparateBackupRestoreAndCsvActions() {
        openDataManagement()

        composeRule.onNodeWithTag("backup_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("restore_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("export_csv_btn").assertIsDisplayed()
    }

    @Test
    fun csvSectionCopyStaysDistinctFromFullBackup() {
        openDataManagement()

        composeRule.onNodeWithText("导出本月数据").assertIsDisplayed()
        composeRule.onNodeWithText("备份全部数据").assertIsDisplayed()
        composeRule.onNodeWithText("恢复数据").assertIsDisplayed()
        composeRule.onNodeWithText("不是完整备份", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(".obackup", substring = true).assertIsDisplayed()
    }

    private fun openDataManagement() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_main_screen")
        composeRule.onNodeWithTag("nav_data").performClick()
        waitForTag("settings_data_screen")
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

private fun runShellCommand(command: String) {
    InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(command)
        .use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).readBytes()
        }
}
