package com.peter.overtimecalculator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.peter.overtimecalculator.domain.SeedColor

private val ClayLightColors = lightColorScheme(
    primary = ClayPrimary,
    secondary = ClaySecondary,
    tertiary = ClayAccent,
    error = ClayError,
    background = ClayBackground,
    surface = ClaySurface,
)

private val ClayDarkColors = darkColorScheme(
    primary = ClayPrimary,
    secondary = ClaySecondary,
    tertiary = ClayAccent,
)

private val GeekBlueLightColors = lightColorScheme(
    primary = Color(0xFF0052CC),
    secondary = Color(0xFF00388C),
    tertiary = Color(0xFF0D47A1),
)
private val GeekBlueDarkColors = darkColorScheme(
    primary = Color(0xFF4C9AFF),
    secondary = Color(0xFF2684FF),
    tertiary = Color(0xFF6554C0),
)

private val MintGreenLightColors = lightColorScheme(
    primary = Color(0xFF00BFA5),
    secondary = Color(0xFF00897B),
    tertiary = Color(0xFF004D40),
)
private val MintGreenDarkColors = darkColorScheme(
    primary = Color(0xFF64FFDA),
    secondary = Color(0xFF1DE9B6),
    tertiary = Color(0xFF00BFA5),
)

private val DeepPurpleLightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
)
private val DeepPurpleDarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
)

@Composable
fun OvertimeCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    seedColor: SeedColor = SeedColor.CLAY,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> when (seedColor) {
            SeedColor.CLAY -> ClayDarkColors
            SeedColor.GEEK_BLUE -> GeekBlueDarkColors
            SeedColor.MINT_GREEN -> MintGreenDarkColors
            SeedColor.DEEP_PURPLE -> DeepPurpleDarkColors
        }
        else -> when (seedColor) {
            SeedColor.CLAY -> ClayLightColors
            SeedColor.GEEK_BLUE -> GeekBlueLightColors
            SeedColor.MINT_GREEN -> MintGreenLightColors
            SeedColor.DEEP_PURPLE -> DeepPurpleLightColors
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
