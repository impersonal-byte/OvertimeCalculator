package com.peter.overtimecalculator

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import java.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DayEditorSliderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareDevice() {
        runShellCommand("input keyevent KEYCODE_WAKEUP")
        runShellCommand("wm dismiss-keyguard")
        runShellCommand("input keyevent 82")
    }

    @Test
    fun dayEditorShowsSliderAndPreservesPresetFlow() {
        val editableDate = editableDate()

        composeRule.onNodeWithTag(dayCardTag(editableDate)).performClick()
        waitForTag("day_editor_sheet")

        composeRule.onNodeWithTag("duration_slider").assertIsDisplayed()
        composeRule.onNodeWithTag("center_marker").assertIsDisplayed()
        composeRule.onNodeWithTag("tick_0").assertIsDisplayed()

        composeRule.onNodeWithTag("preset_120").performClick()
        composeRule.onNodeWithTag("editor_duration_value").assertTextContains("2.0h")

        composeRule.onNodeWithTag("clear_duration").performClick()
        composeRule.onNodeWithTag("editor_duration_value").assertTextContains("0.0h")
    }

    @Test
    fun sliderSavesValueAndRespectsNonWorkdayClamp() {
        val editableDate = editableDate()

        composeRule.onNodeWithTag(dayCardTag(editableDate)).performClick()
        waitForTag("day_editor_sheet")

        composeRule.onNodeWithTag("duration_slider")
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(0.75f) }
        composeRule.onNodeWithTag("editor_duration_value").assertTextContains("8.0h")
        composeRule.onNodeWithTag("editor_save").performClick()

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("day_editor_sheet").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(dayCardTag(editableDate)).assertTextContains("8.0h")

        composeRule.onNodeWithTag(dayCardTag(editableDate)).performClick()
        waitForTag("day_editor_sheet")
        composeRule.onNodeWithTag("override_rest_day").performClick()
        composeRule.onAllNodesWithTag("preset_-120").assertCountEquals(0)

        composeRule.onNodeWithTag("clear_duration").performClick()
        composeRule.onNodeWithTag("duration_slider")
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(0f) }
        composeRule.onNodeWithTag("editor_duration_value").assertTextContains("0.0h")
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
