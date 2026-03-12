package com.peter.overtimecalculator

import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.settings.buildThemeOverviewState
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeOverviewStateTest {
    @Test
    fun usesSystemAccentLabelWhenDynamicColorIsAvailableAndEnabled() {
        val state = buildThemeOverviewState(
            appTheme = AppTheme.SYSTEM,
            useDynamicColor = true,
            seedColor = SeedColor.ROSE,
            supportsDynamicColor = true,
        )

        assertEquals("自动", state.modeLabel)
        assertEquals("系统取色", state.paletteLabel)
        assertEquals("动态色彩", state.accentLabel)
    }

    @Test
    fun fallsBackToSelectedPaletteWhenDynamicColorIsUnavailable() {
        val state = buildThemeOverviewState(
            appTheme = AppTheme.DARK,
            useDynamicColor = true,
            seedColor = SeedColor.MINT_GREEN,
            supportsDynamicColor = false,
        )

        assertEquals("深色", state.modeLabel)
        assertEquals("Mint", state.paletteLabel)
        assertEquals("固定色板", state.accentLabel)
    }
}
