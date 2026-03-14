package com.peter.overtimecalculator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainSmokeTest {
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
