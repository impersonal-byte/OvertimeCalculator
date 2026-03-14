package com.peter.overtimecalculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.ZeroDecimal
import java.time.LocalDate
import java.time.YearMonth

private val placeholderHolidayCalendar = HolidayCalendar()
@Composable
internal fun HomeScreen(
    uiState: AppUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
) {
    val displayedCells = remember(uiState.selectedMonth, uiState.dayCells) {
        if (uiState.dayCells.isEmpty()) {
            buildPlaceholderCells(uiState.selectedMonth)
        } else {
            uiState.dayCells
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SummaryCard(uiState, displayedCells)
        MonthSwitcher(uiState.selectedMonth, onPreviousMonth, onNextMonth)
        CalendarGrid(
            selectedMonth = uiState.selectedMonth,
            dayCells = displayedCells,
            calendarStartDay = uiState.calendarStartDay,
            onDayClick = onDayClick,
            modifier = Modifier.weight(1f),
        )
    }
}


private fun buildPlaceholderCells(selectedMonth: YearMonth): List<DayCellUiState> {
    return (1..selectedMonth.lengthOfMonth()).map { day ->
        val date = selectedMonth.atDay(day)
        DayCellUiState(
            date = date,
            overtimeMinutes = 0,
            dayType = placeholderHolidayCalendar.resolveDayType(date, null),
            pay = ZeroDecimal,
        )
    }
}
