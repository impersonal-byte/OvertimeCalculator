package com.peter.overtimecalculator.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.peter.overtimecalculator.OvertimeApplication
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.DomainResult
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlySummaryUiState
import com.peter.overtimecalculator.domain.ObservedMonth
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayEditorUiState(
    val date: LocalDate,
    val currentMinutes: Int,
    val currentOverride: DayType?,
)

data class AppUiState(
    val selectedMonth: YearMonth,
    val dayCells: List<DayCellUiState>,
    val summary: MonthlySummaryUiState,
    val config: MonthlyConfig,
    val calendarStartDay: CalendarStartDay,
    val editor: DayEditorUiState? = null,
) {
    companion object {
        fun empty(): AppUiState {
            val month = YearMonth.now()
            val config = MonthlyConfig(
                yearMonth = month,
                hourlyRate = ZeroDecimal,
                rateSource = HourlyRateSource.MANUAL,
                weekdayRate = java.math.BigDecimal("1.50"),
                restDayRate = java.math.BigDecimal("2.00"),
                holidayRate = java.math.BigDecimal("3.00"),
                lockedByUser = false,
            )
            return AppUiState(
                selectedMonth = month,
                dayCells = emptyList(),
                summary = MonthlySummaryUiState(
                    totalMinutes = 0,
                    totalPay = ZeroDecimal,
                    yearMonth = month,
                    hourlyRate = ZeroDecimal,
                    rateSource = HourlyRateSource.MANUAL,
                ),
                config = config,
                calendarStartDay = CalendarStartDay.MONDAY,
            )
        }
    }
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent

    data object TriggerHaptic : UiEvent
}

enum class CalendarStartDay {
    MONDAY,
    SUNDAY,
}

@OptIn(ExperimentalCoroutinesApi::class)
class OvertimeViewModel(application: Application) : AndroidViewModel(application) {
    private val appContainer = (application as OvertimeApplication).appContainer
    private val repository = appContainer.repository
    private val saveOvertimeUseCase = appContainer.saveOvertimeUseCase
    private val updateManualHourlyRateUseCase = appContainer.updateManualHourlyRateUseCase
    private val updateMultipliersUseCase = appContainer.updateMultipliersUseCase
    private val reverseEngineerHourlyRateUseCase = appContainer.reverseEngineerHourlyRateUseCase
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedEditorDate = MutableStateFlow<LocalDate?>(null)
    private val sharedPreferences = application.getSharedPreferences("overtime-preferences", Context.MODE_PRIVATE)
    private val calendarStartDay = MutableStateFlow(loadCalendarStartDay())
    private val _events = Channel<UiEvent>(Channel.BUFFERED)

    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private val observedMonth = selectedMonth
        .flatMapLatest(repository::observeMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState: StateFlow<AppUiState> = combine(
        selectedMonth,
        observedMonth,
        selectedEditorDate,
        calendarStartDay,
    ) { month: YearMonth,
        observed: ObservedMonth?,
        editorDate: LocalDate?,
        currentCalendarStartDay: CalendarStartDay ->
        if (observed == null) {
            AppUiState.empty().copy(
                selectedMonth = month,
                calendarStartDay = currentCalendarStartDay,
            )
        } else {
            AppUiState(
                selectedMonth = month,
                dayCells = observed.dayCells,
                summary = observed.summary,
                config = observed.config,
                calendarStartDay = currentCalendarStartDay,
                editor = editorDate?.let { date ->
                    DayEditorUiState(
                        date = date,
                        currentMinutes = observed.entryMinutesByDate[date] ?: 0,
                        currentOverride = observed.overrideTypesByDate[date],
                    )
                },
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppUiState.empty(),
    )

    init {
        ensureMonth(selectedMonth.value)
    }

    fun previousMonth() {
        val month = selectedMonth.value.minusMonths(1)
        selectedMonth.value = month
        selectedEditorDate.value = null
        ensureMonth(month)
    }

    fun nextMonth() {
        val month = selectedMonth.value.plusMonths(1)
        selectedMonth.value = month
        selectedEditorDate.value = null
        ensureMonth(month)
    }

    fun openEditor(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            viewModelScope.launch {
                _events.send(UiEvent.ShowSnackbar("未来日期不能录入加班"))
            }
            return
        }
        selectedEditorDate.value = date
    }

    fun dismissEditor() {
        selectedEditorDate.value = null
    }

    fun updateCalendarStartDay(startDay: CalendarStartDay) {
        calendarStartDay.value = startDay
        sharedPreferences.edit()
            .putString(KEY_CALENDAR_START_DAY, startDay.name)
            .apply()
    }

    fun saveOvertime(date: LocalDate, hoursText: String, minutesText: String, overrideDayType: DayType?) {
        viewModelScope.launch {
            val hours = hoursText.ifBlank { "0" }.toIntOrNull()
            val minutes = minutesText.ifBlank { "0" }.toIntOrNull()
            if (hours == null || minutes == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的工时"))
                return@launch
            }
            if (hours < 0 || minutes < 0) {
                _events.send(UiEvent.ShowSnackbar("时长不能为负数"))
                return@launch
            }
            if (minutes >= 60) {
                _events.send(UiEvent.ShowSnackbar("分钟请输入 0 到 59"))
                return@launch
            }
            handleWriteResult(
                result = saveOvertimeUseCase(date, hours * 60 + minutes, overrideDayType),
                successMessage = null,
                closeEditorOnSuccess = true,
            )
        }
    }

    fun saveOvertimeMinutes(date: LocalDate, totalMinutes: Int, overrideDayType: DayType?) {
        viewModelScope.launch {
            handleWriteResult(
                result = saveOvertimeUseCase(date, totalMinutes, overrideDayType),
                successMessage = null,
                closeEditorOnSuccess = true,
            )
        }
    }

    fun updateManualHourlyRate(rateText: String) {
        viewModelScope.launch {
            val hourlyRate = rateText.toBigDecimalOrNull()
            if (hourlyRate == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的时薪"))
                return@launch
            }
            handleWriteResult(
                result = updateManualHourlyRateUseCase(selectedMonth.value, hourlyRate),
                successMessage = "已保存时薪",
            )
        }
    }

    fun updateMultipliers(weekday: String, restDay: String, holiday: String) {
        viewModelScope.launch {
            val weekdayRate = weekday.toBigDecimalOrNull()
            val restDayRate = restDay.toBigDecimalOrNull()
            val holidayRate = holiday.toBigDecimalOrNull()
            if (weekdayRate == null || restDayRate == null || holidayRate == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的倍率"))
                return@launch
            }
            handleWriteResult(
                result = updateMultipliersUseCase(
                    yearMonth = selectedMonth.value,
                    weekdayRate = weekdayRate,
                    restDayRate = restDayRate,
                    holidayRate = holidayRate,
                ),
                successMessage = "已保存倍率",
            )
        }
    }

    fun reverseEngineerHourlyRate(overtimePayText: String) {
        viewModelScope.launch {
            val overtimePay = overtimePayText.toBigDecimalOrNull()
            if (overtimePay == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的已发加班工资总额"))
                return@launch
            }
            when (val result = reverseEngineerHourlyRateUseCase(selectedMonth.value, overtimePay)) {
                is DomainResult.Success -> {
                    _events.send(
                        UiEvent.ShowSnackbar(
                            "反推时薪 ¥${result.value.hourlyRate.toDisplayString()}，加权工时 ${result.value.weightedHours.toDisplayString()}h",
                        ),
                    )
                    _events.send(UiEvent.TriggerHaptic)
                }
                is DomainResult.Failure -> {
                    _events.send(UiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    private fun ensureMonth(month: YearMonth) {
        viewModelScope.launch {
            repository.ensureMonthExists(month)
        }
    }

    private suspend fun handleWriteResult(
        result: DomainResult<Unit>,
        successMessage: String?,
        closeEditorOnSuccess: Boolean = false,
    ) {
        when (result) {
            is DomainResult.Success -> {
                if (closeEditorOnSuccess) {
                    selectedEditorDate.value = null
                }
                if (successMessage != null) {
                    _events.send(UiEvent.ShowSnackbar(successMessage))
                }
                _events.send(UiEvent.TriggerHaptic)
            }
            is DomainResult.Failure -> {
                _events.send(UiEvent.ShowSnackbar(result.message))
            }
        }
    }

    private fun loadCalendarStartDay(): CalendarStartDay {
        return sharedPreferences.getString(KEY_CALENDAR_START_DAY, CalendarStartDay.MONDAY.name)
            ?.let { stored -> CalendarStartDay.entries.firstOrNull { it.name == stored } }
            ?: CalendarStartDay.MONDAY
    }

    companion object {
        private const val KEY_CALENDAR_START_DAY = "calendar_start_day"

        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return OvertimeViewModel(application) as T
                }
            }
        }
    }
}
