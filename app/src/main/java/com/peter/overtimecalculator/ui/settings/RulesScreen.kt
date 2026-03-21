package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.theme.OvertimeTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(
    uiState: AppUiState,
    onSaveHourlyRate: (String) -> Unit,
    onSaveMultipliers: (String, String, String) -> Unit,
    onReverseEngineer: (String) -> Unit,
    onModeSwitch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaults = OvertimeTheme.defaults
    var selectedModeName by rememberSaveable(uiState.config.yearMonth, uiState.config.rateSource) {
        mutableStateOf(uiState.config.rateSource.toInputMode().name)
    }
    val selectedMode = HourlyRateInputMode.valueOf(selectedModeName)

    var hourlyRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.hourlyRate) {
        mutableStateOf(if (uiState.config.hourlyRate == ZeroDecimal) "" else uiState.config.hourlyRate.toDisplayString())
    }
    var weekdayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.weekdayRate) {
        mutableStateOf(uiState.config.weekdayRate.toDisplayString())
    }
    var restDayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.restDayRate) {
        mutableStateOf(uiState.config.restDayRate.toDisplayString())
    }
    var holidayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.holidayRate) {
        mutableStateOf(uiState.config.holidayRate.toDisplayString())
    }
    var reversePayText by rememberSaveable(uiState.config.yearMonth) { mutableStateOf("") }
    var showMultiplierSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = "薪酬与规则",
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
                .testTag("settings_rules_screen")
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                HourlyRateControlSection(
                    selectedMode = selectedMode,
                    hourlyRateText = hourlyRateText,
                    reversePayText = reversePayText,
                    totalMinutes = uiState.summary.totalMinutes,
                    onModeSelected = { mode ->
                        if (selectedMode != mode) {
                            selectedModeName = mode.name
                            onModeSwitch()
                        }
                    },
                    onHourlyRateTextChange = { hourlyRateText = it.filterAllowedDecimal() },
                    onSaveHourlyRate = { onSaveHourlyRate(hourlyRateText) },
                    onReversePayTextChange = { reversePayText = it.filterAllowedDecimal() },
                    onReverseEngineer = { onReverseEngineer(reversePayText) },
                )
            }
            item {
                MultiplierSection(
                    weekdayRateText = weekdayRateText,
                    restDayRateText = restDayRateText,
                    holidayRateText = holidayRateText,
                    onEditMultipliers = { showMultiplierSheet = true },
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (showMultiplierSheet) {
            MultiplierEditorSheet(
                weekdayRateText = weekdayRateText,
                restDayRateText = restDayRateText,
                holidayRateText = holidayRateText,
                onDismiss = { showMultiplierSheet = false },
                onWeekdayRateChange = { weekdayRateText = it.filterAllowedDecimal() },
                onRestDayRateChange = { restDayRateText = it.filterAllowedDecimal() },
                onHolidayRateChange = { holidayRateText = it.filterAllowedDecimal() },
                onResetDefaults = {
                    weekdayRateText = "1.50"
                    restDayRateText = "2.00"
                    holidayRateText = "3.00"
                },
                onSave = {
                    onSaveMultipliers(weekdayRateText, restDayRateText, holidayRateText)
                    showMultiplierSheet = false
                },
            )
        }
    }
}

private fun HourlyRateSource.toInputMode(): HourlyRateInputMode = when (this) {
    HourlyRateSource.MANUAL -> HourlyRateInputMode.MANUAL
    HourlyRateSource.REVERSE_ENGINEERED -> HourlyRateInputMode.REVERSE
}
