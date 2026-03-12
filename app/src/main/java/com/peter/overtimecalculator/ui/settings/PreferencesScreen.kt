package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.CalendarStartDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    uiState: AppUiState,
    onCalendarStartDayChange: (CalendarStartDay) -> Unit,
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
                SettingCard("日历显示", "选择日历每周的起始日，修改后会立即同步到首页。") {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_start_day_row"),
                    ) {
                        CalendarStartDay.entries.forEachIndexed { index, option ->
                            SegmentedButton(
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
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
