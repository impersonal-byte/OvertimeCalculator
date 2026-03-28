package com.peter.overtimecalculator.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.theme.OvertimeTheme
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
    val defaults = OvertimeTheme.defaults
    val subtleTextColor = defaults.pageForeground.copy(alpha = 0.72f)
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
        containerColor = defaults.pageBackground,
        contentColor = defaults.pageForeground,
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
                ThemeColorSection(
                    supportsDynamicColor = supportsDynamicColor,
                    effectiveDynamicColor = effectiveDynamicColor,
                    paletteSpecs = paletteSpecs,
                    selectedSeedColor = uiState.seedColor,
                    onUseDynamicColorChange = onUseDynamicColorChange,
                    onSeedColorChange = onSeedColorChange,
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = defaults.cardContainer,
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
                            color = subtleTextColor,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
