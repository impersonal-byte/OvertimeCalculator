package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.AppUpdateUiState
import com.peter.overtimecalculator.ui.CalendarStartDay

@Composable
fun SettingsMainScreen(
    uiState: AppUiState,
    updateUiState: AppUpdateUiState,
    onBack: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_main_screen")
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                CategoryNavigationCard(
                    title = "薪酬与规则",
                    subtitle = "当前时薪 $currentRateText ($rateSourceText) · 工作日 ${uiState.config.weekdayRate.toDisplayString()}x",
                    icon = Icons.Default.MonetizationOn,
                    onClick = onNavigateToRules,
                    testTag = "nav_rules",
                )
            }

            item {
                CategoryNavigationCard(
                    title = "偏好设置",
                    subtitle = startDayText,
                    icon = Icons.Default.Tune,
                    onClick = onNavigateToPreferences,
                    testTag = "nav_preferences",
                )
            }

            item {
                CategoryNavigationCard(
                    title = "数据管理",
                    subtitle = "导出、备份与恢复功能预留",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToData,
                    testTag = "nav_data",
                )
            }

            item {
                CategoryNavigationCard(
                    title = "关于加薪",
                    subtitle = aboutStatusSubtitle(updateUiState),
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAbout,
                    testTag = "nav_about",
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CategoryNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "进入详情",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
