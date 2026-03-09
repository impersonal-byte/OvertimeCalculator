package com.peter.overtimecalculator

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

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
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("settings_screen")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("hourly_rate_input").assertIsDisplayed()

        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("home_screen")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun canSaveOvertimeEntryFromCalendar() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("settings_screen")).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("hourly_rate_input").performTextClearance()
        composeRule.onNodeWithTag("hourly_rate_input").performTextInput("50")
        composeRule.onNodeWithTag("save_hourly_rate").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("home_screen")).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("day_card_2026-03-03").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("day_editor_sheet")).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("editor_hours").performTextClearance()
        composeRule.onNodeWithTag("editor_hours").performTextInput("2")
        composeRule.onNodeWithTag("editor_minutes").performTextClearance()
        composeRule.onNodeWithTag("editor_minutes").performTextInput("0")
        composeRule.onNodeWithTag("editor_save").performClick()

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasTag("day_editor_sheet")).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag("day_card_2026-03-03").assertTextContains("2h 0m")
    }
}

private fun hasTag(tag: String) = hasTestTag(tag)
