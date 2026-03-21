package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.AppUpdateUiState
import com.peter.overtimecalculator.ui.CalendarStartDay
import com.peter.overtimecalculator.ui.theme.OvertimeTheme

@Composable
fun SettingsMainScreen(
    uiState: AppUiState,
    updateUiState: AppUpdateUiState,
    onBack: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaults = OvertimeTheme.defaults
    val currentRateText = if (uiState.config.hourlyRate <= ZeroDecimal) {
        "未设置"
    } else {
        "¥${uiState.config.hourlyRate.toDisplayString()}"
    }
    val rateSourceText = when (uiState.config.rateSource) {
        HourlyRateSource.MANUAL -> "手动"
        HourlyRateSource.REVERSE_ENGINEERED -> "反推"
    }
    val startDayText = if (uiState.calendarStartDay == CalendarStartDay.MONDAY) {
        "周一为首日"
    } else {
        "周日为首日"
    }

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = "设置",
                onBack = onBack,
            )
        },
        containerColor = defaults.pageBackground,
        contentColor = defaults.pageForeground,
        modifier = modifier,
    ) { innerPadding ->
        LazySettingsColumn(innerPadding = innerPadding) {
            CategoryNavigationCard(
                title = "主题与外观",
                subtitle = themeSubtitle(uiState),
                icon = Icons.Default.Palette,
                onClick = onNavigateToTheme,
                testTag = "nav_theme",
            )
            CategoryNavigationCard(
                title = "薪资与规则",
                subtitle = "当前时薪 $currentRateText ($rateSourceText) · 工作日 ${uiState.config.weekdayRate.toDisplayString()}x",
                icon = Icons.Default.MonetizationOn,
                onClick = onNavigateToRules,
                testTag = "nav_rules",
            )
            CategoryNavigationCard(
                title = "偏好设置",
                subtitle = startDayText,
                icon = Icons.Default.Tune,
                onClick = onNavigateToPreferences,
                testTag = "nav_preferences",
            )
            CategoryNavigationCard(
                title = "数据管理",
                subtitle = "导出本月 CSV 数据并通过系统分享发送",
                icon = Icons.Default.Settings,
                onClick = onNavigateToData,
                testTag = "nav_data",
            )
            CategoryNavigationCard(
                title = "关于加薪",
                subtitle = aboutStatusSubtitle(updateUiState),
                icon = Icons.Default.Info,
                onClick = onNavigateToAbout,
                testTag = "nav_about",
            )
        }
    }
}
