package com.peter.overtimecalculator.ui.settings

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.theme.ThemePaletteSpec
import com.peter.overtimecalculator.ui.theme.ThemePaletteSpecs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeSettingsScreen(
    uiState: AppUiState,
    onAppThemeChange: (AppTheme) -> Unit,
    onUseDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (SeedColor) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val effectiveDynamicColor = supportsDynamicColor && uiState.useDynamicColor
    val paletteSpecs = ThemePaletteSpecs.all
    val activePalette = ThemePaletteSpecs.fromSeedColor(uiState.seedColor)
    val overviewState = buildThemeOverviewState(
        appTheme = uiState.appTheme,
        useDynamicColor = uiState.useDynamicColor,
        seedColor = uiState.seedColor,
        supportsDynamicColor = supportsDynamicColor,
    )

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = "主题与外观",
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_theme_screen")
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                ThemeOverviewCard(
                    state = overviewState,
                    activePalette = activePalette,
                    modifier = Modifier.testTag("theme_overview_card"),
                )
            }
            item {
                ThemeSectionHeader(
                    title = "主题模式",
                    body = "选择浅色、深色或自动跟随系统，修改后立即生效。",
                )
            }
            item {
                ThemeModeChooser(
                    selectedTheme = uiState.appTheme,
                    activePalette = activePalette,
                    onThemeSelected = onAppThemeChange,
                )
            }
            item {
                ThemeSectionHeader(
                    title = "色彩",
                    body = "动态色彩优先使用系统配色，关闭后恢复到你选中的固定色板。",
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            selectedSeedColor = uiState.seedColor,
                            enabled = !effectiveDynamicColor,
                            onSeedColorSelected = onSeedColorChange,
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "使用说明",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = overviewState.supportingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeOverviewCard(
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
private fun ThemeSectionHeader(
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

@Composable
private fun ThemeModeChooser(
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
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
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
    val tag = "theme_mode_${option.testTagSuffix}"
    Card(
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 0.46f else 0.26f),
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
                color = MaterialTheme.colorScheme.surface.copy(alpha = if (selected) 0.92f else 0.68f),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                },
            ) {
                Text(
                    text = if (selected) "当前使用" else "点击切换",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
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
                lineColor = Color(0xFFD8E0F3),
                outline = Color(0xFFE6E7EF),
            )
            AppTheme.DARK -> drawThemePreviewLayer(
                background = paletteSpec.darkColorScheme.surface,
                panel = Color(0xFF0D1324),
                accent = paletteSpec.darkPreviewAccent,
                lineColor = Color(0xFF3E4C6D),
                outline = Color(0xFF2E3445),
            )
            AppTheme.SYSTEM -> drawSystemPreview(paletteSpec)
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    },
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
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
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            lineColor = Color(0xFFD8E0F3),
            outline = Color(0xFFE6E7EF),
        )
    }
    clipPath(darkPath) {
        drawThemePreviewLayer(
            background = paletteSpec.darkColorScheme.surface,
            panel = Color(0xFF0D1324),
            accent = paletteSpec.darkPreviewAccent,
            lineColor = Color(0xFF3E4C6D),
            outline = Color(0xFF2E3445),
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
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.minDimension * 0.03f),
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
