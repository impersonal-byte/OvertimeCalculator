package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun ExportDataSection(
    onExportDataClick: () -> Unit,
) {
    SettingCard(
        title = "导出本月数据",
        subtitle = "将当前月的加班与调休明细导出为 CSV 表格，并通过系统分享发送。",
    ) {
        Button(
            onClick = onExportDataClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("export_csv_btn"),
        ) {
            Text("导出本月 CSV 数据")
        }
    }
}
