package com.peter.overtimecalculator.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.CalendarStartDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    uiState: AppUiState,
    onCalendarStartDayChange: (CalendarStartDay) -> Unit,
    onAppThemeChange: (AppTheme) -> Unit,
    onUseDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (SeedColor) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            SettingsTopBar(
                title = "偏好设置",
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_preferences_screen")
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                SettingCard("日历显示", "选择日历每周的起始日，修改后会立刻同步到首页。") {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_start_day_row"),
                    ) {
                        CalendarStartDay.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                modifier = Modifier.testTag(
                                    if (option == CalendarStartDay.MONDAY) {
                                        "calendar_start_monday"
                                    } else {
                                        "calendar_start_sunday"
                                    },
                                ),
                                selected = uiState.calendarStartDay == option,
                                onClick = { onCalendarStartDayChange(option) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = CalendarStartDay.entries.size,
                                ),
                            ) {
                                Text(if (option == CalendarStartDay.MONDAY) "周一为首日" else "周日为首日")
                            }
                        }
                    }
                }
            }

            item {
                SettingCard("深浅模式", "选择使用跟随系统、始终浅色或始终深色的界面模式。") {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_theme_row"),
                    ) {
                        val options = listOf(
                            AppTheme.SYSTEM to "跟随系统",
                            AppTheme.LIGHT to "浅色",
                            AppTheme.DARK to "深色",
                        )
                        options.forEachIndexed { index, option ->
                            SegmentedButton(
                                modifier = Modifier.testTag(
                                    when (option.first) {
                                        AppTheme.SYSTEM -> "app_theme_system"
                                        AppTheme.LIGHT -> "app_theme_light"
                                        AppTheme.DARK -> "app_theme_dark"
                                    },
                                ),
                                selected = uiState.appTheme == option.first,
                                onClick = { onAppThemeChange(option.first) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            ) {
                                Text(option.second)
                            }
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    SettingCard("动态配色", "提取当前壁纸的主色调融入应用界面。关闭后可使用原生种子色。") {
                        Switch(
                            checked = uiState.useDynamicColor,
                            onCheckedChange = onUseDynamicColorChange,
                            modifier = Modifier.testTag("dynamic_color_switch"),
                        )
                    }
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !uiState.useDynamicColor) {
                item {
                    SettingCard(
                        title = "个性化种子色",
                        subtitle = "为应用指定一套原生的配色底座。",
                        modifier = Modifier.testTag("seed_color_section"),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val seedColorMapping = mapOf(
                                SeedColor.CLAY to Color(0xFF9A3412),
                                SeedColor.GEEK_BLUE to Color(0xFF0052CC),
                                SeedColor.MINT_GREEN to Color(0xFF00BFA5),
                                SeedColor.DEEP_PURPLE to Color(0xFF6750A4),
                            )
                            SeedColor.entries.forEach { seed ->
                                val color = seedColorMapping[seed] ?: Color.Gray
                                val isSelected = uiState.seedColor == seed
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(color, CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape,
                                        )
                                        .clickable { onSeedColorChange(seed) }
                                        .testTag("seed_color_${seed.name.lowercase()}"),
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
