package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.theme.OvertimeTheme
import com.peter.overtimecalculator.ui.theme.ThemePaletteSpec

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemeColorSection(
    supportsDynamicColor: Boolean,
    effectiveDynamicColor: Boolean,
    paletteSpecs: List<ThemePaletteSpec>,
    selectedSeedColor: SeedColor,
    onUseDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (SeedColor) -> Unit,
) {
    val defaults = OvertimeTheme.defaults
    val subtleTextColor = defaults.pageForeground.copy(alpha = 0.72f)
    Surface(
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 1.dp,
        color = defaults.sectionContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "动态色彩",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (supportsDynamicColor) {
                            "跟随系统壁纸与强调色"
                        } else {
                            "需要 Android 12 及以上版本"
                        },
                        color = subtleTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Switch(
                    checked = effectiveDynamicColor,
                    onCheckedChange = {
                        if (supportsDynamicColor) {
                            onUseDynamicColorChange(it)
                        }
                    },
                    enabled = supportsDynamicColor,
                    modifier = Modifier.testTag("dynamic_color_switch"),
                )
            }
            ThemePaletteGrid(
                paletteSpecs = paletteSpecs,
                selectedSeedColor = selectedSeedColor,
                enabled = !effectiveDynamicColor,
                onSeedColorSelected = onSeedColorChange,
            )
        }
    }
}

@Composable
private fun ThemePaletteGrid(
    paletteSpecs: List<ThemePaletteSpec>,
    selectedSeedColor: SeedColor,
    enabled: Boolean,
    onSeedColorSelected: (SeedColor) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_palette_grid")
            .alpha(if (enabled) 1f else 0.45f),
        maxItemsInEachRow = 4,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        paletteSpecs.forEach { palette ->
            ThemePaletteItem(
                paletteSpec = palette,
                selected = selectedSeedColor == palette.seedColor,
                enabled = enabled,
                onClick = { onSeedColorSelected(palette.seedColor) },
            )
        }
    }
}

@Composable
private fun ThemePaletteItem(
    paletteSpec: ThemePaletteSpec,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaults = OvertimeTheme.defaults
    val subtleTextColor = defaults.pageForeground.copy(alpha = 0.72f)
    Column(
        modifier = modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(if (selected) 1.04f else 1f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (selected) {
                        defaults.accent.copy(alpha = 0.2f)
                    } else {
                        defaults.cardContainer
                    },
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) defaults.accent else defaults.outline.copy(alpha = 0.42f),
                    shape = RoundedCornerShape(24.dp),
                )
                .clickable(enabled = enabled, onClick = onClick)
                .testTag("theme_palette_${paletteSpec.seedColor.name.lowercase()}"),
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val halfWidth = size.width / 2f
                val halfHeight = size.height / 2f
                val corner = size.minDimension * 0.18f
                drawRoundRect(
                    color = paletteSpec.swatchColors[0],
                    size = Size(halfWidth, halfHeight),
                    cornerRadius = CornerRadius(corner, corner),
                )
                drawRoundRect(
                    color = paletteSpec.swatchColors[1],
                    topLeft = Offset(halfWidth, 0f),
                    size = Size(halfWidth, halfHeight),
                    cornerRadius = CornerRadius(corner, corner),
                )
                drawRoundRect(
                    color = paletteSpec.swatchColors[2],
                    topLeft = Offset(0f, halfHeight),
                    size = Size(halfWidth, halfHeight),
                    cornerRadius = CornerRadius(corner, corner),
                )
                drawRoundRect(
                    color = paletteSpec.swatchColors[3],
                    topLeft = Offset(halfWidth, halfHeight),
                    size = Size(halfWidth, halfHeight),
                    cornerRadius = CornerRadius(corner, corner),
                )
            }
        }
        Text(
            text = paletteSpec.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) defaults.accent else subtleTextColor,
        )
    }
}
