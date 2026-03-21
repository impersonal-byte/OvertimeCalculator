package com.peter.overtimecalculator.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

@Immutable
data class ThemeDefaults(
    val pageBackground: Color,
    val pageForeground: Color,
    val navigationContainer: Color,
    val sectionContainer: Color,
    val cardContainer: Color,
    val cardElevatedContainer: Color,
    val outline: Color,
    val accent: Color,
    val accentOn: Color,
    val positiveTint: Color,
    val warningTint: Color,
)

internal fun buildThemeDefaults(colorScheme: ColorScheme): ThemeDefaults {
    val isDark = colorScheme.background.luminance() < 0.5f
    val lightLayerBase = colorScheme.primaryContainer
    return ThemeDefaults(
        pageBackground = colorScheme.background,
        pageForeground = colorScheme.onBackground,
        navigationContainer = if (isDark) {
            colorScheme.surfaceVariant.copy(alpha = 0.42f)
        } else {
            lerp(colorScheme.surface, lightLayerBase, 0.46f)
        },
        sectionContainer = if (isDark) {
            colorScheme.surface
        } else {
            lerp(colorScheme.surface, lightLayerBase, 0.34f)
        },
        cardContainer = if (isDark) {
            colorScheme.surfaceVariant.copy(alpha = 0.28f)
        } else {
            lerp(colorScheme.surface, lightLayerBase, 0.24f)
        },
        cardElevatedContainer = if (isDark) {
            colorScheme.surface.copy(alpha = 0.92f)
        } else {
            lerp(colorScheme.surface, lightLayerBase, 0.54f)
        },
        outline = if (isDark) {
            colorScheme.outlineVariant
        } else {
            lerp(colorScheme.outlineVariant, colorScheme.outline, 0.62f)
        },
        accent = colorScheme.primary,
        accentOn = colorScheme.onPrimary,
        positiveTint = colorScheme.tertiary,
        warningTint = colorScheme.secondary,
    )
}

internal val LocalThemeDefaults = staticCompositionLocalOf<ThemeDefaults> {
    error("ThemeDefaults not provided")
}

object OvertimeTheme {
    val defaults: ThemeDefaults
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeDefaults.current
}
