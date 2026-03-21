package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.CalendarStartDay
import com.peter.overtimecalculator.ui.theme.OvertimeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    uiState: AppUiState,
    onCalendarStartDayChange: (CalendarStartDay) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaults = OvertimeTheme.defaults

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = "偏好设置",
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
                .testTag("settings_preferences_screen")
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                CalendarStartDaySection(
                    selectedStartDay = uiState.calendarStartDay,
                    onCalendarStartDayChange = onCalendarStartDayChange,
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
