package com.peter.overtimecalculator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.peter.overtimecalculator.domain.SeedColor
import com.google.android.material.color.utilities.Hct
import kotlin.math.max
import kotlin.math.min

data class ThemePaletteSpec(
    val seedColor: SeedColor,
    val displayName: String,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme,
    val swatchColors: List<Color>,
    val lightPreviewAccent: Color,
    val darkPreviewAccent: Color,
    val lightPreviewLine: Color,
    val lightPreviewOutline: Color,
    val darkPreviewPanel: Color,
    val darkPreviewLine: Color,
    val darkPreviewOutline: Color,
)

private data class ThemeSeedDefinition(
    val seedColor: SeedColor,
    val displayName: String,
    val seedArgb: Int,
)

object ThemePaletteSpecs {
    private val seedDefinitions = listOf(
        ThemeSeedDefinition(SeedColor.CLAY, "Clay", 0xFFD4A373.toInt()),
        ThemeSeedDefinition(SeedColor.MINT_GREEN, "Mint", 0xFF2C8C74.toInt()),
        ThemeSeedDefinition(SeedColor.AQUA, "Aqua", 0xFF1E88A8.toInt()),
        ThemeSeedDefinition(SeedColor.SKY_BLUE, "Sky", 0xFF4F83CC.toInt()),
        ThemeSeedDefinition(SeedColor.LAVENDER, "Lavender", 0xFF7E8BD8.toInt()),
        ThemeSeedDefinition(SeedColor.ORCHID, "Orchid", 0xFFA27AD6.toInt()),
        ThemeSeedDefinition(SeedColor.LILAC, "Lilac", 0xFFB58AD9.toInt()),
        ThemeSeedDefinition(SeedColor.ROSE, "Rose", 0xFFD777A7.toInt()),
    )

    val all: List<ThemePaletteSpec> = seedDefinitions.map(::generatePalette)

    fun fromSeedColor(seedColor: SeedColor): ThemePaletteSpec {
        return all.firstOrNull { it.seedColor == seedColor } ?: all.first()
    }
}

private fun generatePalette(definition: ThemeSeedDefinition): ThemePaletteSpec {
    val seedHct = Hct.fromInt(definition.seedArgb)
    val primaryChroma = max(36.0, seedHct.chroma)
    val secondaryChroma = min(24.0, primaryChroma * 0.45)
    val tertiaryHue = (seedHct.hue + 50.0) % 360.0
    val tertiaryChroma = min(28.0, primaryChroma * 0.5)
    val neutralChroma = 8.0

    val lightPrimary = hct(seedHct.hue, primaryChroma, 40.0)
    val lightSecondary = hct(seedHct.hue, secondaryChroma, 45.0)
    val lightTertiary = hct(tertiaryHue, tertiaryChroma, 45.0)
    val lightBackground = hct(seedHct.hue, neutralChroma, 98.0)
    val lightSurface = hct(seedHct.hue, neutralChroma, 99.0)

    val darkPrimary = hct(seedHct.hue, primaryChroma, 80.0)
    val darkSecondary = hct(seedHct.hue, secondaryChroma, 76.0)
    val darkTertiary = hct(tertiaryHue, tertiaryChroma, 80.0)
    val darkBackground = hct(seedHct.hue, neutralChroma, 11.0)
    val darkSurface = hct(seedHct.hue, neutralChroma, 16.0)

    return ThemePaletteSpec(
        seedColor = definition.seedColor,
        displayName = definition.displayName,
        lightColorScheme = lightColorScheme(
            primary = lightPrimary,
            secondary = lightSecondary,
            tertiary = lightTertiary,
            background = lightBackground,
            surface = lightSurface,
            surfaceVariant = lightBackground.copy(alpha = 0.92f),
            primaryContainer = hct(seedHct.hue, primaryChroma, 90.0),
        ),
        darkColorScheme = darkColorScheme(
            primary = darkPrimary,
            secondary = darkSecondary,
            tertiary = darkTertiary,
            background = darkBackground,
            surface = darkSurface,
            surfaceVariant = darkSurface.copy(alpha = 0.9f),
            primaryContainer = hct(seedHct.hue, primaryChroma, 30.0),
        ),
        swatchColors = listOf(
            hct(seedHct.hue, primaryChroma, 90.0),
            hct(seedHct.hue, primaryChroma, 75.0),
            hct(seedHct.hue, primaryChroma, 60.0),
            hct(seedHct.hue, primaryChroma, 45.0),
        ),
        lightPreviewAccent = lightSecondary,
        darkPreviewAccent = darkPrimary,
        lightPreviewLine = Color(0xFFD8E0F3),
        lightPreviewOutline = Color(0xFFE6E7EF),
        darkPreviewPanel = Color(0xFF0D1324),
        darkPreviewLine = Color(0xFF3E4C6D),
        darkPreviewOutline = Color(0xFF2E3445),
    )
}

private fun hct(hue: Double, chroma: Double, tone: Double): Color {
    return Color(Hct.from(hue, chroma, tone).toInt())
}
