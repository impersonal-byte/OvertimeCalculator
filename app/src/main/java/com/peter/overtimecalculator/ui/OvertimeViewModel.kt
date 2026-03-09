package com.peter.overtimecalculator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.peter.overtimecalculator.OvertimeApplication
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlySummaryUiState
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    val editor: DayEditorUiState? = null,
    val message: String? = null,
    val feedbackSignal: Long = 0L,
) {
    companion object {
        fun empty(): AppUiState {
            val month = YearMonth.now()
            val config = MonthlyConfig(
                yearMonth = month,
                hourlyRate = 0.0,
                rateSource = HourlyRateSource.MANUAL,
                weekdayRate = 1.5,
                restDayRate = 2.0,
                holidayRate = 3.0,
                lockedByUser = false,
            )
            return AppUiState(
                selectedMonth = month,
                dayCells = emptyList(),
                summary = MonthlySummaryUiState(
                    totalMinutes = 0,
                    totalPay = 0.0,
                    yearMonth = month,
                    hourlyRate = 0.0,
                    rateSource = HourlyRateSource.MANUAL,
                ),
                config = config,
            )
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OvertimeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as OvertimeApplication).appContainer.repository
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedEditorDate = MutableStateFlow<LocalDate?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val feedbackSignal = MutableStateFlow(0L)

    private val observedMonth = selectedMonth
        .flatMapLatest(repository::observeMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState: StateFlow<AppUiState> = combine(
        selectedMonth,
        observedMonth,
        selectedEditorDate,
        message,
        feedbackSignal,
    ) { month, observed, editorDate, snackbar, tickSignal ->
        if (observed == null) {
            AppUiState.empty().copy(
                selectedMonth = month,
                message = snackbar,
                feedbackSignal = tickSignal,
            )
        } else {
            AppUiState(
                selectedMonth = month,
                dayCells = observed.dayCells,
                summary = observed.summary,
                config = observed.config,
                editor = editorDate?.let { date ->
                    DayEditorUiState(
                        date = date,
                        currentMinutes = observed.entryMinutesByDate[date] ?: 0,
                        currentOverride = observed.overrideTypesByDate[date],
                    )
                },
                message = snackbar,
                feedbackSignal = tickSignal,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState.empty())

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
        selectedEditorDate.value = date
    }

    fun dismissEditor() {
        selectedEditorDate.value = null
    }

    fun clearMessage() {
        message.value = null
    }

    fun saveOvertime(date: LocalDate, hoursText: String, minutesText: String, overrideDayType: DayType?) {
        viewModelScope.launch {
            runCatching {
                val hours = hoursText.ifBlank { "0" }.toInt()
                val minutes = minutesText.ifBlank { "0" }.toInt()
                require(hours >= 0 && minutes >= 0) { "时长不能为负数" }
                require(minutes < 60) { "分钟请输入 0 到 59" }
                repository.saveOvertime(date, hours * 60 + minutes, overrideDayType)
            }.onSuccess {
                selectedEditorDate.value = null
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "保存失败"
            }
        }
    }

    fun updateManualHourlyRate(rateText: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateManualHourlyRate(selectedMonth.value, rateText.toDouble())
                "已保存时薪"
            }.onSuccess {
                message.value = it
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "保存时薪失败"
            }
        }
    }

    fun updateMultipliers(weekday: String, restDay: String, holiday: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateMultipliers(
                    yearMonth = selectedMonth.value,
                    weekdayRate = weekday.toDouble(),
                    restDayRate = restDay.toDouble(),
                    holidayRate = holiday.toDouble(),
                )
                "已保存倍率"
            }.onSuccess {
                message.value = it
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "保存倍率失败"
            }
        }
    }

    fun reverseEngineerHourlyRate(overtimePayText: String) {
        viewModelScope.launch {
            runCatching {
                val result = repository.reverseEngineerHourlyRate(
                    selectedMonth.value,
                    overtimePayText.toDouble(),
                )
                "反推时薪 ¥${"%.2f".format(result.hourlyRate)}，加权工时 ${"%.2f".format(result.weightedHours)}h"
            }.onSuccess {
                message.value = it
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "反推时薪失败"
            }
        }
    }

    private fun ensureMonth(month: YearMonth) {
        viewModelScope.launch {
            repository.ensureMonthExists(month)
        }
    }

    private fun emitFeedback() {
        feedbackSignal.value += 1
    }

    companion object {
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
