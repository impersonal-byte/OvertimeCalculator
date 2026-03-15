package com.peter.overtimecalculator

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import java.time.LocalDate
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
        composeRule.onNodeWithTag(dayCardTag(editableDate())).assertIsDisplayed()
    }

    @Test
    fun canNavigateToSettingsAndBack() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_main_screen")

        composeRule.onNodeWithTag("settings_main_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_theme").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_rules").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_preferences").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_data").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_about").assertIsDisplayed()

        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("home_screen")
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun reverseModeOnlyShowsReversePanel() {
        openRulesScreen()

        composeRule.onNodeWithTag("hourly_mode_reverse").performClick()
        waitForTag("reverse_panel")
        composeRule.onNodeWithTag("reverse_panel").assertIsDisplayed()
        composeRule.onAllNodesWithTag("manual_hourly_panel").assertCountEquals(0)
    }

    @Test
    fun canSaveOvertimeEntryFromCalendarAndShowDecimalHours() {
        openRulesScreen()
        ensureManualMode()

        composeRule.onNodeWithTag("hourly_rate_input").performTextClearance()
        composeRule.onNodeWithTag("hourly_rate_input").performTextInput("50")
        composeRule.onNodeWithTag("save_hourly_rate").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("settings_main_screen")
        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("home_screen")

        val editableDate = editableDate()
        composeRule.onNodeWithTag(dayCardTag(editableDate)).performClick()
        waitForTag("day_editor_sheet")

        composeRule.onNodeWithTag("clear_duration").performClick()
        composeRule.onNodeWithTag("duration_slider")
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(0.625f) }
        composeRule.onNodeWithTag("editor_save").performClick()

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("day_editor_sheet").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(dayCardTag(editableDate)).assertTextContains("2.0h")
    }

    @Test
    fun reverseEngineerUpdatesRateSourceChip() {
        val editableDate = editableDate()

        composeRule.onNodeWithTag(dayCardTag(editableDate)).performClick()
        waitForTag("day_editor_sheet")

        composeRule.onNodeWithTag("clear_duration").performClick()
        composeRule.onNodeWithTag("duration_slider")
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(0.625f) }
        composeRule.onNodeWithTag("editor_save").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("day_editor_sheet").fetchSemanticsNodes().isEmpty()
        }

        openRulesScreen()
        composeRule.onNodeWithTag("hourly_mode_reverse").performClick()
        waitForTag("reverse_pay_input")
        composeRule.onNodeWithTag("reverse_pay_input").performTextInput("300")
        composeRule.onNodeWithTag("start_reverse").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("settings_main_screen")
        composeRule.onNodeWithTag("back_button").performClick()
        waitForTag("home_screen")
        composeRule.onNodeWithText("当前时薪·反推", substring = true).assertIsDisplayed()
    }

    @Test
    fun multipliersAreEditedInBottomSheet() {
        openRulesScreen()

        composeRule.onNodeWithTag("edit_multipliers_button").performClick()
        waitForTag("multiplier_editor_sheet")
        composeRule.onNodeWithTag("multiplier_editor_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("weekday_rate_input").performTextClearance()
        composeRule.onNodeWithTag("weekday_rate_input").performTextInput("1.80")
        composeRule.onNodeWithTag("save_multipliers_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("1.80x", substring = true).assertIsDisplayed()
    }

    @Test
    fun dataManagementShowsCsvExportEntry() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_main_screen")

        composeRule.onNodeWithTag("nav_data").performClick()
        waitForTag("settings_data_screen")
        composeRule.onNodeWithTag("settings_data_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("export_csv_btn").assertIsDisplayed()
    }

    @Test
    fun aboutScreenShowsVersionAndUpdateControls() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_main_screen")

        composeRule.onNodeWithTag("nav_about").performClick()
        waitForTag("settings_about_screen")
        composeRule.onNodeWithTag("settings_about_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("current_version_text").assertIsDisplayed()
        composeRule.onNodeWithTag("check_update_button").assertIsDisplayed()
    }

    @Test
    fun themeSettingsShowsPreviewCardsAndPaletteGrid() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_main_screen")

        composeRule.onNodeWithTag("nav_theme").performClick()
        waitForTag("settings_theme_screen")

        composeRule.onNodeWithTag("settings_theme_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_overview_card").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_overview_mode").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_overview_palette").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_mode_light").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_mode_dark").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_mode_system").assertIsDisplayed()
        composeRule.onNodeWithTag("dynamic_color_switch").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_palette_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_palette_rose").assertIsDisplayed()
    }

    @Test
    fun canSwitchThemeModeFromThemeSettings() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_main_screen")

        composeRule.onNodeWithTag("nav_theme").performClick()
        waitForTag("settings_theme_screen")

        composeRule.onNodeWithTag("theme_mode_dark").performClick()
        composeRule.onNodeWithTag("theme_mode_dark_selected").assertIsDisplayed()
    }

    private fun openRulesScreen() {
        composeRule.onNodeWithTag("settings_button").performClick()
        waitForTag("settings_main_screen")
        composeRule.onNodeWithTag("nav_rules").performClick()
        waitForTag("settings_rules_screen")
    }

    private fun ensureManualMode() {
        if (composeRule.onAllNodesWithTag("hourly_rate_input").fetchSemanticsNodes().isEmpty()) {
            composeRule.onNodeWithTag("hourly_mode_manual").performClick()
            waitForTag("hourly_rate_input")
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun editableDate(): LocalDate {
        val today = LocalDate.now()
        return today.withDayOfMonth(minOf(3, today.dayOfMonth))
    }

    private fun dayCardTag(date: LocalDate): String = "day_card_$date"
}

private fun runShellCommand(command: String) {
    InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(command)
        .use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).readBytes()
        }
}
