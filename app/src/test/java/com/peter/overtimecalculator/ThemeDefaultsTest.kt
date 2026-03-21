package com.peter.overtimecalculator

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.peter.overtimecalculator.ui.theme.buildThemeDefaults
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ThemeDefaultsTest {
    @Test
    fun darkThemeDefaultsKeepLayerSeparation() {
        val defaults = buildThemeDefaults(
            darkColorScheme(
                background = Color(0xFF12131A),
                surface = Color(0xFF181A24),
                surfaceVariant = Color(0xFF23263A),
                primary = Color(0xFFB8C4FF),
                onPrimary = Color(0xFF11152A),
                secondary = Color(0xFFF2C38C),
                tertiary = Color(0xFF9FE3C5),
            ),
        )

        assertNotEquals(defaults.pageBackground, defaults.sectionContainer)
        assertNotEquals(defaults.pageBackground, defaults.cardContainer)
        assertTrue(defaults.navigationContainer.alpha > 0f)
    }

    @Test
    fun lightThemeDefaultsExposeUsableAccentRoles() {
        val defaults = buildThemeDefaults(
            lightColorScheme(
                background = Color(0xFFF8F8FB),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFE5E8F1),
                primary = Color(0xFF3256D0),
                onPrimary = Color.White,
                secondary = Color(0xFFD78A2F),
                tertiary = Color(0xFF2A9C77),
            ),
        )

        assertNotEquals(defaults.pageBackground, defaults.sectionContainer)
        assertNotEquals(defaults.pageBackground, defaults.cardContainer)
        assertNotEquals(defaults.sectionContainer, defaults.cardContainer)
        assertNotEquals(defaults.sectionContainer, defaults.cardElevatedContainer)
        assertTrue(abs(defaults.pageBackground.luminance() - defaults.sectionContainer.luminance()) >= 0.03f)
        assertTrue(abs(defaults.sectionContainer.luminance() - defaults.cardContainer.luminance()) >= 0.02f)
        assertTrue(abs(defaults.cardContainer.luminance() - defaults.cardElevatedContainer.luminance()) >= 0.03f)
        assertNotEquals(defaults.accent, defaults.accentOn)
        assertTrue(defaults.outline.alpha > 0f)
        assertTrue(defaults.warningTint.alpha > 0f)
        assertTrue(defaults.positiveTint.alpha > 0f)
    }
}
