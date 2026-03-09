package com.peter.overtimecalculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.ui.theme.ClayAccent
import com.peter.overtimecalculator.ui.theme.ClayPrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val HomeRoute = "home"
private const val SettingsRoute = "settings"
private val placeholderHolidayCalendar = HolidayCalendar()

@Composable
fun OvertimeCalculatorApp(viewModel: OvertimeViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: HomeRoute

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
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
                if (currentRoute == HomeRoute) {
                    "加班工资计算器"
                } else {
                    "${selectedMonth.format(DateTimeFormatter.ofPattern("yyyy 年 M 月"))}设置"
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Text(
                    uiState.selectedMonth.format(DateTimeFormatter.ofPattern("yyyy 年 M 月")),
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
                label = {
                    Text(
                        when (uiState.config.rateSource) {
                            HourlyRateSource.MANUAL -> "时薪来源：手动输入"
                            HourlyRateSource.REVERSE_ENGINEERED -> "时薪来源：工资反推"
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
                    text = "当前时薪为 0，请先到设置页手动录入或用已发加班工资反推时薪。",
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
    Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
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
                selectedMonth.format(DateTimeFormatter.ofPattern("yyyy 年 M 月")),
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date == null) {
                            Spacer(modifier = Modifier.fillMaxWidth().aspectRatio(0.86f))
                        } else {
                            DayCard(dayMap.getValue(date)) { onDayClick(date) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCard(cell: DayCellUiState, onClick: () -> Unit) {
    val accent = when (cell.dayType) {
        DayType.WORKDAY -> MaterialTheme.colorScheme.primary
        DayType.REST_DAY -> MaterialTheme.colorScheme.secondary
        DayType.HOLIDAY -> ClayAccent
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.86f)
            .testTag("day_card_${cell.date}")
            .clickable(onClick = onClick)
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(cell.date.dayOfMonth.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Box(modifier = Modifier.size(8.dp).background(accent, CircleShape))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dayTypeLabel(cell.dayType), style = MaterialTheme.typography.labelSmall, color = accent)
                Text(
                    if (cell.overtimeMinutes > 0) formatMinutes(cell.overtimeMinutes) else "未记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (cell.pay > 0) "¥${"%.0f".format(cell.pay)}" else "¥0",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (cell.pay > 0) ClayPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

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
    var overrideType by rememberSaveable(editor.date) { mutableStateOf(editor.currentOverride) }

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
                "当前系统判定：${dayTypeLabel(resolvedDayType)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = { hourText = it.filter(Char::isDigit) },
                    label = { Text("小时") },
                    modifier = Modifier.weight(1f).testTag("editor_hours"),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = { minuteText = it.filter(Char::isDigit) },
                    label = { Text("分钟") },
                    modifier = Modifier.weight(1f).testTag("editor_minutes"),
                    singleLine = true,
                )
            }
            Text("日期类型覆盖", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OverrideChip("跟随系统", overrideType == null) { overrideType = null }
                DayType.entries.forEach { type ->
                    OverrideChip(dayTypeLabel(type), overrideType == type) { overrideType = type }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(hourText, minuteText, overrideType) },
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
) {
    var hourlyRateText by remember(uiState.config.yearMonth, uiState.config.hourlyRate) {
        mutableStateOf(if (uiState.config.hourlyRate == 0.0) "" else "%.2f".format(uiState.config.hourlyRate))
    }
    var weekdayRateText by remember(uiState.config.yearMonth, uiState.config.weekdayRate) {
        mutableStateOf("%.2f".format(uiState.config.weekdayRate))
    }
    var restDayRateText by remember(uiState.config.yearMonth, uiState.config.restDayRate) {
        mutableStateOf("%.2f".format(uiState.config.restDayRate))
    }
    var holidayRateText by remember(uiState.config.yearMonth, uiState.config.holidayRate) {
        mutableStateOf("%.2f".format(uiState.config.holidayRate))
    }
    var reversePayText by remember(uiState.config.yearMonth) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            SettingCard("时薪设置", "手动输入本月时薪；会作为之后未锁定月份的默认值") {
                OutlinedTextField(
                    value = hourlyRateText,
                    onValueChange = { hourlyRateText = it.filterAllowedDecimal() },
                    label = { Text("时薪（元/小时）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("hourly_rate_input"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onSaveHourlyRate(hourlyRateText) },
                    modifier = Modifier.testTag("save_hourly_rate"),
                ) {
                    Text("保存时薪")
                }
            }
        }
        item {
            SettingCard("倍率设置", "工作日 / 休息日 / 节假日的工资倍数") {
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
            SettingCard("反推时薪", "输入本月已发加班工资，系统会根据已录入的每日明细和倍率反推时薪") {
                OutlinedTextField(
                    value = reversePayText,
                    onValueChange = { reversePayText = it.filterAllowedDecimal() },
                    label = { Text("已发加班工资总额") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reverse_pay_input"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "当前月已录入 ${formatMinutes(uiState.summary.totalMinutes)}。若没有每日明细，反推会被阻止。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onReverseEngineer(reversePayText) },
                    modifier = Modifier.testTag("start_reverse"),
                ) {
                    Text("开始反推")
                }
            }
        }
        item {
            SettingCard("节假日说明", "优先级：手动覆盖 > 内置节假日/调休 > 周末 > 普通工作日") {
                Text(
                    "内置 2026 年官方调休数据，并为 2027-2030 预留节假日日期；超出范围自动回退为周末规则。任何一天都可以在首页录入时手动覆盖类型。",
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
