package com.peter.overtimecalculator

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareDevice() {
        runShellCommand("input keyevent KEYCODE_WAKEUP")
        runShellCommand("wm dismiss-keyguard")
        runShellCommand("input keyevent 82")
    }

    @Test
    fun homeScreenShowsSummaryAndCalendar() {
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("summary_card").assertIsDisplayed()
        composeRule.onNodeWithTag("calendar_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("day_card_2026-03-03").assertIsDisplayed()
    }

    @Test
    fun canNavigateToSettingsAndBack() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_screen")
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("hourly_mode_row").assertIsDisplayed()
        composeRule.onNodeWithTag("current_version_text").assertIsDisplayed()
        composeRule.onNodeWithTag("check_update_button").assertIsDisplayed()

        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("home_screen")
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun reverseModeOnlyShowsReversePanel() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_screen")

        composeRule.onNodeWithTag("hourly_mode_reverse").performClick()
        waitForTag("reverse_panel")
        composeRule.onNodeWithTag("reverse_panel").assertIsDisplayed()
        composeRule.onAllNodesWithTag("manual_hourly_panel").assertCountEquals(0)
    }

    @Test
    fun canSaveOvertimeEntryFromCalendarAndShowDecimalHours() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_screen")
        ensureManualMode()

        composeRule.onNodeWithTag("hourly_rate_input").performTextClearance()
        composeRule.onNodeWithTag("hourly_rate_input").performTextInput("50")
        composeRule.onNodeWithTag("save_hourly_rate").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("home_screen")

        composeRule.onNodeWithTag("day_card_2026-03-03").performClick()
        waitForTag("day_editor_sheet")

        composeRule.onNodeWithTag("editor_hours").performTextClearance()
        composeRule.onNodeWithTag("editor_hours").performTextInput("2")
        composeRule.onNodeWithTag("editor_minutes").performTextClearance()
        composeRule.onNodeWithTag("editor_minutes").performTextInput("0")
        composeRule.onNodeWithTag("editor_save").performClick()

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("day_editor_sheet")).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag("day_card_2026-03-03").assertTextContains("2.0h")
    }

    @Test
    fun reverseEngineerUpdatesRateSourceChip() {
        composeRule.onNodeWithTag("day_card_2026-03-03").performClick()
        waitForTag("day_editor_sheet")

        composeRule.onNodeWithTag("editor_hours").performTextClearance()
        composeRule.onNodeWithTag("editor_hours").performTextInput("2")
        composeRule.onNodeWithTag("editor_minutes").performTextClearance()
        composeRule.onNodeWithTag("editor_minutes").performTextInput("0")
        composeRule.onNodeWithTag("editor_save").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("day_editor_sheet")).fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_screen")

        composeRule.onNodeWithTag("hourly_mode_reverse").performClick()
        waitForTag("reverse_pay_input")
        composeRule.onNodeWithTag("reverse_pay_input").performTextInput("300")
        composeRule.onNodeWithTag("start_reverse").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("home_screen")
        composeRule.onNodeWithText("时薪来源：总额反推", substring = true).assertIsDisplayed()
    }

    private fun ensureManualMode() {
        if (composeRule.onAllNodesWithTag("hourly_rate_input").fetchSemanticsNodes().isEmpty()) {
            composeRule.onNodeWithTag("hourly_mode_manual").performClick()
            waitForTag("hourly_rate_input")
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag(tag)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

private fun hasTag(tag: String) = hasTestTag(tag)

private fun runShellCommand(command: String) {
    InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(command)
        .use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).readBytes()
        }
}
