package com.peter.overtimecalculator

import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.theme.ThemePaletteSpecs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteSpecTest {
    @Test
    fun themePaletteSpecsCoverAllSeedColors() {
        assertEquals(8, SeedColor.entries.size)
        assertEquals(SeedColor.entries.toSet(), ThemePaletteSpecs.all.map { it.seedColor }.toSet())
    }

    @Test
    fun rosePaletteProvidesPreviewColors() {
        val rose = ThemePaletteSpecs.fromSeedColor(SeedColor.ROSE)
        assertEquals("Rose", rose.displayName)
        assertTrue(rose.lightPreviewAccent != rose.darkPreviewAccent)
    }

    @Test
    fun darkPaletteUsesLayeredGrayAndAvoidsPureBlackBackground() {
        val rose = ThemePaletteSpecs.fromSeedColor(SeedColor.ROSE)
        val dark = rose.darkColorScheme

        assertNotEquals(dark.background, dark.surface)
        assertNotEquals(0xFF000000.toInt(), dark.background.toArgb())
    }

    @Test
    fun previewNeutralFieldsReturnCanonicalColors() {
        val spec = ThemePaletteSpecs.fromSeedColor(SeedColor.ROSE)

        // Light preview neutrals
        assertEquals(Color(0xFFD8E0F3), spec.lightPreviewLine)
        assertEquals(Color(0xFFE6E7EF), spec.lightPreviewOutline)

        // Dark preview neutrals
        assertEquals(Color(0xFF0D1324), spec.darkPreviewPanel)
        assertEquals(Color(0xFF3E4C6D), spec.darkPreviewLine)
        assertEquals(Color(0xFF2E3445), spec.darkPreviewOutline)
    }
}
