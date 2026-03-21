package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.ui.theme.OvertimeTheme
import com.peter.overtimecalculator.ui.theme.ThemePaletteSpec

@Composable
internal fun ThemeModeChooser(
    selectedTheme: AppTheme,
    activePalette: ThemePaletteSpec,
    onThemeSelected: (AppTheme) -> Unit,
) {
    val entries = listOf(
        ThemeModeOption(AppTheme.LIGHT, "浅色", "light", "明亮、清爽、信息层次更轻"),
        ThemeModeOption(AppTheme.DARK, "深色", "dark", "更稳重，适合夜间和低亮度"),
        ThemeModeOption(AppTheme.SYSTEM, "自动", "system", "跟随系统时段与全局设置"),
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        if (availableWidth >= 360.dp) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                entries.forEach { option ->
                    ThemeModePreviewCard(
                        option = option,
                        paletteSpec = activePalette,
                        selected = selectedTheme == option.theme,
                        onClick = { onThemeSelected(option.theme) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                entries.forEach { option ->
                    ThemeModePreviewCard(
                        option = option,
                        paletteSpec = activePalette,
                        selected = selectedTheme == option.theme,
                        onClick = { onThemeSelected(option.theme) },
                        modifier = Modifier.width(availableWidth * 0.72f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModePreviewCard(
    option: ThemeModeOption,
    paletteSpec: ThemePaletteSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaults = OvertimeTheme.defaults
    val subtleTextColor = defaults.pageForeground.copy(alpha = 0.72f)
    val tag = "theme_mode_${option.testTagSuffix}"
    Card(
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) defaults.accent else defaults.outline.copy(alpha = 0.42f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = defaults.sectionContainer.copy(alpha = if (selected) 0.92f else 0.72f),
        ),
        modifier = modifier
            .testTag(tag)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = defaults.cardContainer.copy(alpha = if (selected) 1f else 0.84f),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.94f),
            ) {
                ThemeModeThumbnail(
                    option = option,
                    paletteSpec = paletteSpec,
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = option.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtleTextColor,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) {
                    defaults.accent.copy(alpha = 0.18f)
                } else {
                    defaults.navigationContainer
                },
            ) {
                Text(
                    text = if (selected) "当前使用" else "点击切换",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        defaults.accent
                    } else {
                        subtleTextColor
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            if (selected) {
                Box(modifier = Modifier.size(1.dp).testTag("${tag}_selected"))
            }
        }
    }
}

@Composable
private fun ThemeModeThumbnail(
    option: ThemeModeOption,
    paletteSpec: ThemePaletteSpec,
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        when (option.theme) {
            AppTheme.LIGHT -> drawThemePreviewLayer(
                background = paletteSpec.lightColorScheme.surface,
                panel = paletteSpec.lightColorScheme.background,
                accent = paletteSpec.lightPreviewAccent,
                lineColor = paletteSpec.lightPreviewLine,
                outline = paletteSpec.lightPreviewOutline,
            )
            AppTheme.DARK -> drawThemePreviewLayer(
                background = paletteSpec.darkColorScheme.surface,
                panel = paletteSpec.darkPreviewPanel,
                accent = paletteSpec.darkPreviewAccent,
                lineColor = paletteSpec.darkPreviewLine,
                outline = paletteSpec.darkPreviewOutline,
            )
            AppTheme.SYSTEM -> drawSystemPreview(paletteSpec)
        }
    }
}

private fun DrawScope.drawSystemPreview(paletteSpec: ThemePaletteSpec) {
    val lightPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(0f, size.height)
        close()
    }
    val darkPath = Path().apply {
        moveTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    clipPath(lightPath) {
        drawThemePreviewLayer(
            background = paletteSpec.lightColorScheme.surface,
            panel = paletteSpec.lightColorScheme.background,
            accent = paletteSpec.lightPreviewAccent,
            lineColor = paletteSpec.lightPreviewLine,
            outline = paletteSpec.lightPreviewOutline,
        )
    }
    clipPath(darkPath) {
        drawThemePreviewLayer(
            background = paletteSpec.darkColorScheme.surface,
            panel = paletteSpec.darkPreviewPanel,
            accent = paletteSpec.darkPreviewAccent,
            lineColor = paletteSpec.darkPreviewLine,
            outline = paletteSpec.darkPreviewOutline,
        )
    }
    drawLine(
        color = Color.White.copy(alpha = 0.9f),
        start = Offset(size.width, 0f),
        end = Offset(0f, size.height),
        strokeWidth = size.minDimension * 0.03f,
    )
}

private fun DrawScope.drawThemePreviewLayer(
    background: Color,
    panel: Color,
    accent: Color,
    lineColor: Color,
    outline: Color,
) {
    val corner = size.minDimension * 0.12f
    drawRoundRect(
        color = background,
        size = size,
        cornerRadius = CornerRadius(corner, corner),
    )
    drawRoundRect(
        color = outline,
        size = size,
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = size.minDimension * 0.03f),
    )
    val inset = size.width * 0.12f
    drawRoundRect(
        color = panel,
        topLeft = Offset(inset, size.height * 0.12f),
        size = Size(size.width * 0.62f, size.height * 0.24f),
        cornerRadius = CornerRadius(corner * 0.45f, corner * 0.45f),
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.78f),
        topLeft = Offset(inset * 1.05f, size.height * 0.14f),
        size = Size(size.width * 0.16f, size.height * 0.21f),
        cornerRadius = CornerRadius(corner * 0.3f, corner * 0.3f),
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.42f),
        topLeft = Offset(inset * 2.15f, size.height * 0.14f),
        size = Size(size.width * 0.16f, size.height * 0.21f),
        cornerRadius = CornerRadius(corner * 0.3f, corner * 0.3f),
    )
    drawRoundRect(
        color = lineColor,
        topLeft = Offset(inset, size.height * 0.46f),
        size = Size(size.width * 0.48f, size.height * 0.08f),
        cornerRadius = CornerRadius(corner * 0.28f, corner * 0.28f),
    )
    drawRoundRect(
        color = lineColor.copy(alpha = 0.72f),
        topLeft = Offset(inset, size.height * 0.58f),
        size = Size(size.width * 0.38f, size.height * 0.08f),
        cornerRadius = CornerRadius(corner * 0.28f, corner * 0.28f),
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.52f),
        topLeft = Offset(inset, size.height * 0.72f),
        size = Size(size.width * 0.56f, size.height * 0.09f),
        cornerRadius = CornerRadius(corner * 0.28f, corner * 0.28f),
    )
    drawCircle(
        color = accent.copy(alpha = 0.92f),
        radius = size.minDimension * 0.085f,
        center = Offset(size.width * 0.2f, size.height * 0.88f),
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.76f),
        topLeft = Offset(size.width * 0.36f, size.height * 0.81f),
        size = Size(size.width * 0.36f, size.height * 0.14f),
        cornerRadius = CornerRadius(corner * 0.45f, corner * 0.45f),
    )
}

private data class ThemeModeOption(
    val theme: AppTheme,
    val label: String,
    val testTagSuffix: String,
    val summary: String,
)
