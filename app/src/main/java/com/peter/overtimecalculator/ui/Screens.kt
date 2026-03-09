package com.peter.overtimecalculator.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import com.peter.overtimecalculator.domain.UpdateUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val HomeRoute = "home"
private const val SettingsRoute = "settings"
private val placeholderHolidayCalendar = HolidayCalendar()
private val monthFormatter = DateTimeFormatter.ofPattern("yyyy 年 M 月", Locale.CHINA)

private enum class HourlyRateInputMode(val label: String) {
    MANUAL("手动输入"),
    REVERSE("总额反推"),
}

@Composable
fun OvertimeCalculatorApp(viewModel: OvertimeViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val tickHaptic = rememberTickHapticFeedback()
    val lifecycleOwner = LocalLifecycleOwner.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: HomeRoute

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    LaunchedEffect(uiState.feedbackSignal) {
        if (uiState.feedbackSignal > 0L) {
            tickHaptic.performTick()
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onHostResumed()
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
                    onSaveHourlyRate = viewModel::updateManualHourlyRate,
                    onSaveMultipliers = viewModel::updateMultipliers,
                    onReverseEngineer = viewModel::reverseEngineerHourlyRate,
                    onCheckForUpdates = viewModel::checkForUpdates,
                    onModeSwitch = tickHaptic::performTick,
                )
            }
        }
    }

    uiState.editor?.let { editor ->
        DayEditorSheet(
            editor = editor,
            resolvedDayType = uiState.dayCells.firstOrNull { it.date == editor.date }?.dayType ?: DayType.WORKDAY,
            onDismiss = viewModel::dismissEditor,
            onSave = { hours, minutes, overrideDayType ->
                viewModel.saveOvertime(editor.date, hours, minutes, overrideDayType)
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
                    "${selectedMonth.format(monthFormatter)}设置"
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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item { SummaryCard(uiState) }
        item { MonthSwitcher(uiState.selectedMonth, onPreviousMonth, onNextMonth) }
        item { CalendarGrid(uiState.selectedMonth, displayedCells, onDayClick) }
        item { Spacer(modifier = Modifier.height(12.dp)) }
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Text(
                    uiState.selectedMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                "¥${"%.2f".format(uiState.summary.totalPay)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.testTag("summary_total_pay"),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric("累计时长", formatMinutes(uiState.summary.totalMinutes))
                SummaryMetric("当前时薪", "¥${"%.2f".format(uiState.config.hourlyRate)}")
            }
            AssistChip(
                onClick = {},
                enabled = false,
                modifier = Modifier.testTag("rate_source_chip"),
                label = {
                    Text(
                        when (uiState.config.rateSource) {
                            HourlyRateSource.MANUAL -> "时薪来源：手动输入"
                            HourlyRateSource.REVERSE_ENGINEERED -> "时薪来源：总额反推"
                        },
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
            if (uiState.config.hourlyRate <= 0.0) {
                Text(
                    text = "当前时薪为 0，请先到设置页手动输入，或根据已发加班工资总额进行反推。",
                    style = MaterialTheme.typography.bodyMedium,
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
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
            }
            Text(
                selectedMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleLarge,
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
    onDayClick: (LocalDate) -> Unit,
) {
    val dayMap = remember(dayCells) { dayCells.associateBy { it.date } }
    val presentationMap = remember(dayCells) { buildCalendarCellPresentations(dayCells) }
    val leadingBlanks = remember(selectedMonth) { selectedMonth.atDay(1).dayOfWeek.toCalendarOffset() }
    val totalCells = leadingBlanks + selectedMonth.lengthOfMonth()
    val trailingBlanks = if (totalCells % 7 == 0) 0 else 7 - (totalCells % 7)
    val slots = buildList<LocalDate?> {
        repeat(leadingBlanks) { add(null) }
        dayCells.forEach { add(it.date) }
        repeat(trailingBlanks) { add(null) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("calendar_grid"),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        slots.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date == null) {
                            Spacer(modifier = Modifier.fillMaxWidth().aspectRatio(0.9f))
                        } else {
                            DayCard(
                                cell = dayMap.getValue(date),
                                presentation = presentationMap.getValue(date),
                                onClick = { onDayClick(date) },
                            )
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
    onClick: () -> Unit,
) {
    val colors = dayCardColors(presentation)

    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = if (presentation.tier == CalendarCellIntensityTier.NONE) 1.dp else 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .testTag("day_card_${cell.date}")
            .clickable(onClick = onClick)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = cell.date.dayOfMonth.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
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
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.content,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
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
    onSave: (String, String, DayType?) -> Unit,
) {
    var hourText by rememberSaveable(editor.date) { mutableStateOf((editor.currentMinutes / 60).toString()) }
    var minuteText by rememberSaveable(editor.date) { mutableStateOf((editor.currentMinutes % 60).toString()) }
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
            Text(
                "系统判定：${dayTypeLabel(resolvedDayType)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = { hourText = it.filter(Char::isDigit) },
                    label = { Text("小时") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_hours"),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = { minuteText = it.filter(Char::isDigit) },
                    label = { Text("分钟") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_minutes"),
                    singleLine = true,
                )
            }
            Text("日期类型覆盖", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OverrideChip("跟随系统", overrideType.isBlank()) { overrideType = "" }
                DayType.entries.forEach { type ->
                    OverrideChip(dayTypeLabel(type), overrideType == type.name) { overrideType = type.name }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSave(hourText, minuteText, overrideType.takeIf { it.isNotBlank() }?.let(DayType::valueOf))
                    },
                    modifier = Modifier.testTag("editor_save"),
                ) {
                    Text("保存")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OverrideChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun SettingsScreen(
    uiState: AppUiState,
    onSaveHourlyRate: (String) -> Unit,
    onSaveMultipliers: (String, String, String) -> Unit,
    onReverseEngineer: (String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onModeSwitch: () -> Unit,
) {
    var selectedModeName by rememberSaveable(uiState.config.yearMonth, uiState.config.rateSource) {
        mutableStateOf(uiState.config.rateSource.toInputMode().name)
    }
    val selectedMode = HourlyRateInputMode.valueOf(selectedModeName)

    var hourlyRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.hourlyRate) {
        mutableStateOf(if (uiState.config.hourlyRate == 0.0) "" else "%.2f".format(uiState.config.hourlyRate))
    }
    var weekdayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.weekdayRate) {
        mutableStateOf("%.2f".format(uiState.config.weekdayRate))
    }
    var restDayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.restDayRate) {
        mutableStateOf("%.2f".format(uiState.config.restDayRate))
    }
    var holidayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.holidayRate) {
        mutableStateOf("%.2f".format(uiState.config.holidayRate))
    }
    var reversePayText by rememberSaveable(uiState.config.yearMonth) { mutableStateOf("") }

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
                OutlinedTextField(
                    value = weekdayRateText,
                    onValueChange = { weekdayRateText = it.filterAllowedDecimal() },
                    label = { Text("工作日倍率") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = restDayRateText,
                    onValueChange = { restDayRateText = it.filterAllowedDecimal() },
                    label = { Text("休息日倍率") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = holidayRateText,
                    onValueChange = { holidayRateText = it.filterAllowedDecimal() },
                    label = { Text("节假日倍率") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onSaveMultipliers(weekdayRateText, restDayRateText, holidayRateText) }) {
                    Text("保存倍率")
                }
            }
        }
        item {
            SettingCard("检查更新", "从 GitHub 最新正式版本检查并下载更新。") {
                Text(
                    text = "当前版本：${uiState.currentVersionName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("current_version_text"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = updateStatusLabel(uiState.updateState, uiState.awaitingInstallPermission),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("update_status_text"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                val isUpdateInProgress = uiState.updateState is UpdateUiState.Checking ||
                    uiState.updateState is UpdateUiState.Downloading
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
                    "当前内置 2026-2030 节假日与调休日数据；超出范围时自动回退为周末规则。任何一天都可以在首页录入时手动覆盖类型。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
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

private fun DayOfWeek.toCalendarOffset(): Int = when (this) {
    DayOfWeek.MONDAY -> 0
    DayOfWeek.TUESDAY -> 1
    DayOfWeek.WEDNESDAY -> 2
    DayOfWeek.THURSDAY -> 3
    DayOfWeek.FRIDAY -> 4
    DayOfWeek.SATURDAY -> 5
    DayOfWeek.SUNDAY -> 6
}

private fun dayTypeLabel(type: DayType): String = when (type) {
    DayType.WORKDAY -> "工作日"
    DayType.REST_DAY -> "休息日"
    DayType.HOLIDAY -> "节假日"
}

private fun formatMinutes(totalMinutes: Int): String = "${totalMinutes / 60}h ${totalMinutes % 60}m"

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
            pay = 0.0,
        )
    }
}
