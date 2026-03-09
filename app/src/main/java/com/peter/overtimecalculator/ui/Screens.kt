package com.peter.overtimecalculator.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import com.peter.overtimecalculator.domain.UpdateUiState
import com.peter.overtimecalculator.ui.CalendarStartDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.withTimeoutOrNull

private const val HomeRoute = "home"
private const val SettingsRoute = "settings"
private val placeholderHolidayCalendar = HolidayCalendar()
private val monthFormatter = DateTimeFormatter.ofPattern("yyyy 年 M 月", Locale.CHINA)

private enum class HourlyRateInputMode(val label: String) {
    MANUAL("手动输入"),
    REVERSE("总额反推"),
}

private const val DurationStepMinutes = 30
private const val MaxOvertimeMinutes = 16 * 60
private const val MinCompMinutes = -8 * 60
private val PositiveDurationPresetsMinutes = listOf(30, 60, 90, 120, 180, 240, 360, 480, 600, 720, 840, 960)
private val NegativeDurationPresetsMinutes = listOf(-30, -60, -120, -240, -480)
private val DurationPresetsMinutes = PositiveDurationPresetsMinutes

@Composable
fun OvertimeCalculatorApp(
    viewModel: OvertimeViewModel,
    appUpdateViewModel: AppUpdateViewModel,
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateUiState by appUpdateViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val tickHaptic = rememberTickHapticFeedback()
    val lifecycleOwner = LocalLifecycleOwner.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: HomeRoute

    LaunchedEffect(updateUiState.message) {
        val message = updateUiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        appUpdateViewModel.clearMessage()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                UiEvent.TriggerHaptic -> tickHaptic.performTick()
            }
        }
    }

    DisposableEffect(lifecycleOwner, appUpdateViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                appUpdateViewModel.onHostResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(
                currentRoute = currentRoute,
                selectedMonth = uiState.selectedMonth,
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(SettingsRoute) },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(HomeRoute) {
                HomeScreen(
                    uiState = uiState,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onDayClick = viewModel::openEditor,
                )
            }
            composable(SettingsRoute) {
                SettingsScreen(
                    uiState = uiState,
                    updateUiState = updateUiState,
                    onSaveHourlyRate = viewModel::updateManualHourlyRate,
                    onSaveMultipliers = viewModel::updateMultipliers,
                    onReverseEngineer = viewModel::reverseEngineerHourlyRate,
                    onCheckForUpdates = appUpdateViewModel::checkForUpdates,
                    onCalendarStartDayChange = viewModel::updateCalendarStartDay,
                    onModeSwitch = tickHaptic::performTick,
                )
            }
        }
    }

    uiState.editor?.let { editor ->
        CompTimeDayEditorSheet(
            editor = editor,
            resolvedDayType = uiState.dayCells.firstOrNull { it.date == editor.date }?.dayType ?: DayType.WORKDAY,
            onDismiss = viewModel::dismissEditor,
            onSave = { totalMinutes, overrideDayType ->
                viewModel.saveOvertimeMinutes(editor.date, totalMinutes, overrideDayType)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentRoute: String,
    selectedMonth: YearMonth,
    onBack: () -> Unit,
    onSettings: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = if (currentRoute == HomeRoute) {
                    "加班工资计算器"
                } else {
                    "设置"
                },
            )
        },
        navigationIcon = {
            if (currentRoute == SettingsRoute) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        },
        actions = {
            if (currentRoute == HomeRoute) {
                IconButton(onClick = onSettings, modifier = Modifier.testTag("settings_button")) {
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                }
            }
        },
    )
}

@Composable
private fun HomeScreen(
    uiState: AppUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
) {
    val displayedCells = if (uiState.dayCells.isEmpty()) {
        buildPlaceholderCells(uiState.selectedMonth)
    } else {
        uiState.dayCells
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SummaryCard(uiState)
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

@Composable
private fun SummaryCard(uiState: AppUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.testTag("summary_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Text(
                    uiState.selectedMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Text(
                "¥${uiState.summary.totalPay.toDisplayString()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.testTag("summary_total_pay"),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric("累计时长", formatMinutes(uiState.summary.totalMinutes))
                SummaryMetric(
                    label = when (uiState.config.rateSource) {
                        HourlyRateSource.MANUAL -> "当前时薪·手动"
                        HourlyRateSource.REVERSE_ENGINEERED -> "当前时薪·反推"
                    },
                    value = "¥${uiState.config.hourlyRate.toDisplayString()}",
                )
            }
            if (uiState.summary.uncoveredCompMinutes > 0) {
                Text(
                    text = "本月调休已超过加班余额，超出 ${formatStepperDuration(uiState.summary.uncoveredCompMinutes)} 暂未计入金额，请按公司规则处理。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (uiState.config.hourlyRate <= ZeroDecimal) {
                Text(
                    text = "时薪未设置，请到设置页录入或反推。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MonthSwitcher(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
            }
            Text(
                selectedMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    selectedMonth: YearMonth,
    dayCells: List<DayCellUiState>,
    calendarStartDay: CalendarStartDay,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayMap = remember(dayCells) { dayCells.associateBy { it.date } }
    val presentationMap = remember(dayCells) { buildCalendarCellPresentations(dayCells) }
    val today = remember { LocalDate.now() }
    val leadingBlanks = remember(selectedMonth, calendarStartDay) {
        selectedMonth.atDay(1).dayOfWeek.toCalendarOffset(calendarStartDay)
    }
    val totalCells = leadingBlanks + selectedMonth.lengthOfMonth()
    val trailingBlanks = if (totalCells % 7 == 0) 0 else 7 - (totalCells % 7)
    val slots = remember(selectedMonth, dayCells, leadingBlanks, trailingBlanks) {
        buildList<LocalDate?> {
            repeat(leadingBlanks) { add(null) }
            dayCells.forEach { add(it.date) }
            repeat(trailingBlanks) { add(null) }
        }
    }
    val weeks = remember(slots) { slots.chunked(7) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.testTag("calendar_grid"),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dayOfWeekLabels(calendarStartDay).forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        week.forEach { date ->
                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                if (date == null) {
                                    Spacer(modifier = Modifier.fillMaxSize())
                                } else {
                                    DayCard(
                                        cell = dayMap.getValue(date),
                                        presentation = presentationMap.getValue(date),
                                        isToday = date == today,
                                        isFuture = date.isAfter(today),
                                        onClick = { onDayClick(date) },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    cell: DayCellUiState,
    presentation: CalendarCellPresentation,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = dayCardColors(presentation)
    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else colors.border
    val borderWidth = if (isToday) 2.dp else 1.dp

    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = if (presentation.tier == CalendarCellIntensityTier.NONE) 1.dp else 3.dp,
        modifier = modifier
            .alpha(if (isFuture) 0.56f else 1f)
            .testTag("day_card_${cell.date}")
            .clickable(enabled = !isFuture, onClick = onClick)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = cell.date.dayOfMonth.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = if (presentation.tier == CalendarCellIntensityTier.NONE) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    colors.content
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (presentation.hoursLabel.isNotBlank()) {
                    Text(
                        text = presentation.hoursLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.content,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun dayCardColors(presentation: CalendarCellPresentation): DayCardColors {
    val scheme = MaterialTheme.colorScheme
    return when (presentation.colorRole) {
        CalendarCellColorRole.DEFAULT -> DayCardColors(
            container = scheme.surface,
            content = scheme.onSurface,
            border = scheme.outlineVariant,
        )
        CalendarCellColorRole.COMP_TIME -> layeredColors(
            tier = presentation.tier,
            base = scheme.surface,
            target = scheme.surfaceVariant,
            content = scheme.onSurfaceVariant,
            border = scheme.outline,
        )
        CalendarCellColorRole.WORKDAY_OVERTIME -> layeredColors(
            tier = presentation.tier,
            base = scheme.surface,
            target = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            border = scheme.secondary,
        )
        CalendarCellColorRole.REST_DAY_OVERTIME -> layeredColors(
            tier = presentation.tier,
            base = scheme.surface,
            target = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
            border = scheme.primary,
        )
        CalendarCellColorRole.HOLIDAY_OVERTIME -> layeredColors(
            tier = presentation.tier,
            base = scheme.surface,
            target = scheme.errorContainer,
            content = scheme.onErrorContainer,
            border = scheme.error,
        )
        CalendarCellColorRole.HOLIDAY_OVERTIME_HIGH -> DayCardColors(
            container = scheme.error,
            content = scheme.onError,
            border = scheme.error,
        )
    }
}

@Composable
private fun layeredColors(
    tier: CalendarCellIntensityTier,
    base: Color,
    target: Color,
    content: Color,
    border: Color,
): DayCardColors {
    val amount = when (tier) {
        CalendarCellIntensityTier.NONE -> 0f
        CalendarCellIntensityTier.LOW -> 0.42f
        CalendarCellIntensityTier.MID -> 0.68f
        CalendarCellIntensityTier.HIGH -> 0.92f
    }
    return DayCardColors(
        container = lerp(base, target, amount),
        content = if (amount >= 0.6f) content else MaterialTheme.colorScheme.onSurface,
        border = lerp(MaterialTheme.colorScheme.outlineVariant, border, 0.75f),
    )
}

private data class DayCardColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DayEditorSheet(
    editor: DayEditorUiState,
    resolvedDayType: DayType,
    onDismiss: () -> Unit,
    onSave: (Int, DayType?) -> Unit,
) {
    var totalMinutes by rememberSaveable(editor.date) { mutableStateOf(roundToNearestHalfHour(editor.currentMinutes)) }
    var overrideType by rememberSaveable(editor.date) { mutableStateOf(editor.currentOverride?.name ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.testTag("day_editor_sheet")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = editor.date.format(DateTimeFormatter.ofPattern("M 月 d 日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("duration_stepper"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("加班时长", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DurationStepperButton(
                            label = "-",
                            enabled = totalMinutes > 0,
                            tag = "decrease_duration",
                        ) {
                            totalMinutes = adjustDurationMinutes(totalMinutes, -DurationStepMinutes)
                        }
                        Text(
                            text = formatStepperDuration(totalMinutes),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_duration_value"),
                        )
                        DurationStepperButton(
                            label = "+",
                            enabled = totalMinutes < MaxOvertimeMinutes,
                            tag = "increase_duration",
                        ) {
                            totalMinutes = adjustDurationMinutes(totalMinutes, DurationStepMinutes)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = { totalMinutes = 0 },
                            modifier = Modifier.testTag("clear_duration"),
                        ) {
                            Text("清零")
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DurationPresetsMinutes.forEach { presetMinutes ->
                            PresetDurationChip(
                                label = formatStepperDuration(presetMinutes),
                                selected = totalMinutes == presetMinutes,
                                tag = "preset_${presetMinutes}",
                            ) {
                                totalMinutes = presetMinutes
                            }
                        }
                    }
                }
            }
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "系统判定",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = dayTypeLabel(resolvedDayType),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "日期类型覆盖",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        OverrideChip("跟随系统", overrideType.isBlank()) { overrideType = "" }
                        DayType.entries.forEach { type ->
                            OverrideChip(dayTypeLabel(type), overrideType == type.name) { overrideType = type.name }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSave(totalMinutes, overrideType.takeIf { it.isNotBlank() }?.let(DayType::valueOf))
                            },
                            modifier = Modifier.testTag("editor_save"),
                        ) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CompTimeDayEditorSheet(
    editor: DayEditorUiState,
    resolvedDayType: DayType,
    onDismiss: () -> Unit,
    onSave: (Int, DayType?) -> Unit,
) {
    var overrideType by rememberSaveable(editor.date) { mutableStateOf(editor.currentOverride?.name ?: "") }
    val effectiveDayType = overrideType.takeIf { it.isNotBlank() }?.let(DayType::valueOf) ?: resolvedDayType
    val minMinutes = minAllowedMinutes(effectiveDayType)
    var totalMinutes by rememberSaveable(editor.date) {
        mutableStateOf(roundSignedToNearestHalfHour(editor.currentMinutes, minMinutes))
    }

    LaunchedEffect(minMinutes) {
        totalMinutes = totalMinutes.coerceIn(minMinutes, MaxOvertimeMinutes)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.testTag("day_editor_sheet")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = editor.date.format(DateTimeFormatter.ofPattern("M 月 d 日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("duration_stepper"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("工时调整", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DurationStepperButton(
                            label = "-",
                            enabled = totalMinutes > minMinutes,
                            tag = "decrease_duration",
                        ) {
                            totalMinutes = adjustSignedDurationMinutes(totalMinutes, -DurationStepMinutes, minMinutes)
                        }
                        Text(
                            text = formatStepperDuration(totalMinutes),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_duration_value"),
                        )
                        DurationStepperButton(
                            label = "+",
                            enabled = totalMinutes < MaxOvertimeMinutes,
                            tag = "increase_duration",
                        ) {
                            totalMinutes = adjustSignedDurationMinutes(totalMinutes, DurationStepMinutes, minMinutes)
                        }
                    }
                    Text(
                        text = when {
                            totalMinutes < 0 -> "当前为调休申请，将优先抵扣本月工作日加班"
                            effectiveDayType == DayType.WORKDAY -> "工作日支持 -8.0h 到 16.0h，负值表示调休。"
                            else -> "当前类型仅支持 0.0h 到 16.0h。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = { totalMinutes = 0 },
                            modifier = Modifier.testTag("clear_duration"),
                        ) {
                            Text("清零")
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (effectiveDayType == DayType.WORKDAY) {
                            NegativeDurationPresetsMinutes.forEach { presetMinutes ->
                                PresetDurationChip(
                                    label = formatStepperDuration(presetMinutes),
                                    selected = totalMinutes == presetMinutes,
                                    tag = "preset_${presetMinutes}",
                                ) {
                                    totalMinutes = presetMinutes
                                }
                            }
                        }
                        PositiveDurationPresetsMinutes.forEach { presetMinutes ->
                            PresetDurationChip(
                                label = formatStepperDuration(presetMinutes),
                                selected = totalMinutes == presetMinutes,
                                tag = "preset_${presetMinutes}",
                            ) {
                                totalMinutes = presetMinutes
                            }
                        }
                    }
                }
            }
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "系统判定",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = dayTypeLabel(resolvedDayType),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "日期类型覆盖",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        OverrideChip("跟随系统", overrideType.isBlank()) { overrideType = "" }
                        DayType.entries.forEach { type ->
                            OverrideChip(dayTypeLabel(type), overrideType == type.name) { overrideType = type.name }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSave(totalMinutes, overrideType.takeIf { it.isNotBlank() }?.let(DayType::valueOf))
                            },
                            modifier = Modifier.testTag("editor_save"),
                        ) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverrideChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun DurationStepperButton(label: String, enabled: Boolean, tag: String, onStep: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .width(60.dp)
            .height(56.dp)
            .testTag(tag)
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (!enabled) {
                            return@detectTapGestures
                        }
                        onStep()
                        val releasedEarly = withTimeoutOrNull(350L) { tryAwaitRelease() }
                        if (releasedEarly == null) {
                            while (true) {
                                onStep()
                                val releasedDuringRepeat = withTimeoutOrNull(90L) { tryAwaitRelease() }
                                if (releasedDuringRepeat != null) {
                                    break
                                }
                            }
                        }
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }
    }
}

@Composable
private fun PresetDurationChip(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(tag),
        label = { Text(label) },
    )
}

@Composable
private fun MultiplierBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text("${value}x", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiState: AppUiState,
    updateUiState: AppUpdateUiState,
    onSaveHourlyRate: (String) -> Unit,
    onSaveMultipliers: (String, String, String) -> Unit,
    onReverseEngineer: (String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onCalendarStartDayChange: (CalendarStartDay) -> Unit,
    onModeSwitch: () -> Unit,
) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            SettingCard("时薪控制中心", "在手动输入和总额反推之间切换，本月配置会随保存结果更新。") {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hourly_mode_row"),
                ) {
                    HourlyRateInputMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = selectedMode == mode,
                            onClick = {
                                if (selectedMode != mode) {
                                    selectedModeName = mode.name
                                    onModeSwitch()
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = HourlyRateInputMode.entries.size,
                            ),
                            modifier = Modifier.testTag(
                                if (mode == HourlyRateInputMode.MANUAL) {
                                    "hourly_mode_manual"
                                } else {
                                    "hourly_mode_reverse"
                                },
                            ),
                        ) {
                            Text(mode.label)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                when (selectedMode) {
                    HourlyRateInputMode.MANUAL -> {
                        Column(
                            modifier = Modifier.testTag("manual_hourly_panel"),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = hourlyRateText,
                                onValueChange = { hourlyRateText = it.filterAllowedDecimal() },
                                label = { Text("时薪（元/小时）") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("hourly_rate_input"),
                            )
                            Button(
                                onClick = { onSaveHourlyRate(hourlyRateText) },
                                modifier = Modifier.testTag("save_hourly_rate"),
                            ) {
                                Text("保存时薪")
                            }
                        }
                    }
                    HourlyRateInputMode.REVERSE -> {
                        Column(
                            modifier = Modifier.testTag("reverse_panel"),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = reversePayText,
                                onValueChange = { reversePayText = it.filterAllowedDecimal() },
                                label = { Text("已发加班工资总额") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reverse_pay_input"),
                            )
                            Text(
                                "时薪 = 总额 / Σ(每日工时 × 当日倍率)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("hourly_formula"),
                            )
                            Text(
                                "当前月已录入 ${formatMinutes(uiState.summary.totalMinutes)}。若没有每日明细，反推会被阻止。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(
                                onClick = { onReverseEngineer(reversePayText) },
                                modifier = Modifier.testTag("start_reverse"),
                            ) {
                                Text("保存并反推")
                            }
                        }
                    }
                }
            }
        }
        item {
            SettingCard("倍率设置", "工作日、休息日、节假日的加班工资倍率。") {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("multiplier_summary_card"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MultiplierBadge("工作日", weekdayRateText)
                    MultiplierBadge("休息日", restDayRateText)
                    MultiplierBadge("节假日", holidayRateText)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { showMultiplierSheet = true },
                        modifier = Modifier.testTag("edit_multipliers_button"),
                    ) {
                        Text("修改倍率")
                    }
                }
            }
        }
        item {
            SettingCard("日历显示", "选择日历每周的起始日。") {
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
                            Text(
                                if (option == CalendarStartDay.MONDAY) {
                                    "周一为首日"
                                } else {
                                    "周日为首日"
                                },
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingCard("检查更新", "从 GitHub 最新正式版本检查并下载更新。") {
                Text(
                    text = "当前版本：${updateUiState.currentVersionName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("current_version_text"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = updateStatusLabel(updateUiState.updateState, updateUiState.awaitingInstallPermission),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("update_status_text"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                val isUpdateInProgress = updateUiState.updateState is UpdateUiState.Checking ||
                    updateUiState.updateState is UpdateUiState.Downloading
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onCheckForUpdates,
                        enabled = !isUpdateInProgress,
                        modifier = Modifier.testTag("check_update_button"),
                    ) {
                        Text("检查更新")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isUpdateInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag("update_progress_indicator"),
                                strokeWidth = 2.5.dp,
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingCard("节假日说明", "优先级：手动覆盖 > 内置节假日/调休 > 周末 > 普通工作日。") {
                Text(
                    "应用内置节假日基线数据，并会通过 Timor 中国节假日 API 静默刷新当前年和下一年规则；当远端无有效数据时，会自动回退到内置基线与周末规则。任何一天都可以在首页录入时手动覆盖类型。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showMultiplierSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMultiplierSheet = false },
            modifier = Modifier.testTag("multiplier_editor_sheet"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("修改倍率", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "默认中国常规值为 1.5 / 2.0 / 3.0。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = weekdayRateText,
                    onValueChange = { weekdayRateText = it.filterAllowedDecimal() },
                    label = { Text("工作日倍率") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekday_rate_input"),
                )
                OutlinedTextField(
                    value = restDayRateText,
                    onValueChange = { restDayRateText = it.filterAllowedDecimal() },
                    label = { Text("休息日倍率") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restday_rate_input"),
                )
                OutlinedTextField(
                    value = holidayRateText,
                    onValueChange = { holidayRateText = it.filterAllowedDecimal() },
                    label = { Text("节假日倍率") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holiday_rate_input"),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            weekdayRateText = "1.50"
                            restDayRateText = "2.00"
                            holidayRateText = "3.00"
                        },
                        modifier = Modifier.testTag("reset_multipliers_button"),
                    ) {
                        Text("恢复默认值")
                    }
                    Button(
                        onClick = {
                            onSaveMultipliers(weekdayRateText, restDayRateText, holidayRateText)
                            showMultiplierSheet = false
                        },
                        modifier = Modifier.testTag("save_multipliers_button"),
                    ) {
                        Text("保存倍率")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

private fun HourlyRateSource.toInputMode(): HourlyRateInputMode = when (this) {
    HourlyRateSource.MANUAL -> HourlyRateInputMode.MANUAL
    HourlyRateSource.REVERSE_ENGINEERED -> HourlyRateInputMode.REVERSE
}

private fun updateStatusLabel(updateState: UpdateUiState, awaitingInstallPermission: Boolean): String {
    return when {
        awaitingInstallPermission -> "请在系统设置中允许安装未知应用，返回后会继续安装。"
        updateState is UpdateUiState.Checking -> "正在检查 GitHub 最新版本…"
        updateState is UpdateUiState.Downloading -> {
            val progress = updateState.progressPercent
            if (progress == null) {
                "正在下载 ${updateState.remoteVersion}…"
            } else {
                "正在下载 ${updateState.remoteVersion}… $progress%"
            }
        }
        updateState is UpdateUiState.UpToDate -> "当前已是最新版本。"
        updateState is UpdateUiState.UpdateAvailable -> "发现新版本 ${updateState.remoteVersion}。"
        updateState is UpdateUiState.ReadyToInstall -> "更新包已准备完成，正在拉起安装。"
        updateState is UpdateUiState.Error -> updateState.message
        updateState is UpdateUiState.Idle -> "点击后会检查 GitHub 最新正式版本，并直接下载更新包。"
        else -> "点击后会检查 GitHub 最新正式版本，并直接下载更新包。"
    }
}

private fun DayOfWeek.toCalendarOffset(calendarStartDay: CalendarStartDay): Int = when (calendarStartDay) {
    CalendarStartDay.MONDAY -> when (this) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }
    CalendarStartDay.SUNDAY -> when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
}

private fun dayOfWeekLabels(calendarStartDay: CalendarStartDay): List<String> {
    return if (calendarStartDay == CalendarStartDay.MONDAY) {
        listOf("一", "二", "三", "四", "五", "六", "日")
    } else {
        listOf("日", "一", "二", "三", "四", "五", "六")
    }
}

private fun dayTypeLabel(type: DayType): String = when (type) {
    DayType.WORKDAY -> "工作日"
    DayType.REST_DAY -> "休息日"
    DayType.HOLIDAY -> "节假日"
}

private fun formatMinutes(totalMinutes: Int): String {
    val sign = if (totalMinutes < 0) "-" else ""
    val absoluteMinutes = kotlin.math.abs(totalMinutes)
    return "$sign${absoluteMinutes / 60}h ${absoluteMinutes % 60}m"
}

private fun formatStepperDuration(totalMinutes: Int): String {
    return String.format(Locale.US, "%.1fh", totalMinutes / 60.0)
}

private fun roundToNearestHalfHour(totalMinutes: Int): Int {
    return roundSignedToNearestHalfHour(totalMinutes, minMinutes = 0)
}

private fun roundSignedToNearestHalfHour(totalMinutes: Int, minMinutes: Int): Int {
    val rounded = if (totalMinutes >= 0) {
        ((totalMinutes + 15) / DurationStepMinutes) * DurationStepMinutes
    } else {
        -(((kotlin.math.abs(totalMinutes) + 15) / DurationStepMinutes) * DurationStepMinutes)
    }
    return rounded.coerceIn(minMinutes, MaxOvertimeMinutes)
}

private fun adjustDurationMinutes(currentMinutes: Int, deltaMinutes: Int): Int {
    return adjustSignedDurationMinutes(currentMinutes, deltaMinutes, minMinutes = 0)
}

private fun adjustSignedDurationMinutes(currentMinutes: Int, deltaMinutes: Int, minMinutes: Int): Int {
    return (currentMinutes + deltaMinutes).coerceIn(minMinutes, MaxOvertimeMinutes)
}

private fun minAllowedMinutes(dayType: DayType): Int {
    return if (dayType == DayType.WORKDAY) MinCompMinutes else 0
}

private fun String.filterAllowedDecimal(): String {
    val raw = filter { it.isDigit() || it == '.' }
    val firstDot = raw.indexOf('.')
    return if (firstDot == -1) raw else raw.substring(0, firstDot + 1) + raw.substring(firstDot + 1).replace(".", "")
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
