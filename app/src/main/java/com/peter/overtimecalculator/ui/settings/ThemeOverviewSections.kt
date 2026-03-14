package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.theme.ThemePaletteSpec
import com.peter.overtimecalculator.ui.theme.ThemePaletteSpecs

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemeOverviewCard(
    state: ThemeOverviewState,
    activePalette: ThemePaletteSpec,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "当前外观",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "把常用状态收在这里，方便快速确认当前模式。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OverviewPill(
                        text = state.modeLabel,
                        testTag = "theme_overview_mode",
                    )
                    OverviewPill(
                        text = state.paletteLabel,
                        testTag = "theme_overview_palette",
                    )
                    OverviewPill(
                        text = state.accentLabel,
                        testTag = "theme_overview_accent",
                    )
                }
            }
            ThemePaletteMiniPreview(paletteSpec = activePalette)
        }
    }
}

@Composable
private fun ThemePaletteMiniPreview(
    paletteSpec: ThemePaletteSpec,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        modifier = modifier.size(86.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val cardCorner = size.minDimension * 0.24f
            drawRoundRect(
                color = paletteSpec.lightColorScheme.surface,
                cornerRadius = CornerRadius(cardCorner, cardCorner),
            )
            val inset = size.width * 0.16f
            val itemSize = Size(size.width * 0.26f, size.height * 0.26f)
            drawRoundRect(
                color = paletteSpec.swatchColors[0],
                topLeft = Offset(inset, inset),
                size = itemSize,
                cornerRadius = CornerRadius(cardCorner * 0.45f, cardCorner * 0.45f),
            )
            drawRoundRect(
                color = paletteSpec.swatchColors[1],
                topLeft = Offset(size.width * 0.58f, inset),
                size = itemSize,
                cornerRadius = CornerRadius(cardCorner * 0.45f, cardCorner * 0.45f),
            )
            drawRoundRect(
                color = paletteSpec.swatchColors[2],
                topLeft = Offset(inset, size.height * 0.58f),
                size = itemSize,
                cornerRadius = CornerRadius(cardCorner * 0.45f, cardCorner * 0.45f),
            )
            drawRoundRect(
                color = paletteSpec.swatchColors[3],
                topLeft = Offset(size.width * 0.58f, size.height * 0.58f),
                size = itemSize,
                cornerRadius = CornerRadius(cardCorner * 0.45f, cardCorner * 0.45f),
            )
        }
    }
}

@Composable
private fun OverviewPill(
    text: String,
    testTag: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        modifier = Modifier.testTag(testTag),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
internal fun ThemeSectionHeader(
    title: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


internal data class ThemeOverviewState(
    val modeLabel: String,
    val paletteLabel: String,
    val accentLabel: String,
    val supportingText: String,
)

internal fun buildThemeOverviewState(
    appTheme: AppTheme,
    useDynamicColor: Boolean,
    seedColor: SeedColor,
    supportsDynamicColor: Boolean,
): ThemeOverviewState {
    val modeLabel = when (appTheme) {
        AppTheme.SYSTEM -> "自动"
        AppTheme.LIGHT -> "浅色"
        AppTheme.DARK -> "深色"
    }
    val dynamicActive = supportsDynamicColor && useDynamicColor
    val paletteLabel = if (dynamicActive) {
        "系统取色"
    } else {
        ThemePaletteSpecs.fromSeedColor(seedColor).displayName
    }
    val accentLabel = if (dynamicActive) "动态色彩" else "固定色板"
    val supportingText = if (dynamicActive) {
        "动态色彩开启后会跟随系统取色，固定色板会被保留，关闭后立即恢复。"
    } else {
        "当前使用固定色板。你切换的主题模式和色板都会立即应用，并在下次启动时保持。"
    }
    return ThemeOverviewState(
        modeLabel = modeLabel,
        paletteLabel = paletteLabel,
        accentLabel = accentLabel,
        supportingText = supportingText,
    )
}
