package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.ui.theme.OvertimeTheme

/**
 * Section for creating a full backup of all app data.
 * This is distinct from CSV export which only covers current month.
 */
@Composable
internal fun BackupSection(
    onBackupClick: () -> Unit,
) {
    val defaults = OvertimeTheme.defaults

    SettingCard(
        title = "备份全部数据",
        subtitle = "创建包含所有月份、加班记录和设置的完整备份文件 (.obackup)。可用于换机或重装后恢复数据。",
    ) {
        Button(
            onClick = onBackupClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backup_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = defaults.accent,
                contentColor = defaults.accentOn,
            ),
        ) {
            Text("创建备份")
        }
    }
}

/**
 * Section for restoring from a backup file.
 * Uses SAF (Storage Access Framework) to let user pick the file.
 */
@Composable
internal fun RestoreSection(
    onRestoreClick: () -> Unit,
) {
    val defaults = OvertimeTheme.defaults

    SettingCard(
        title = "恢复数据",
        subtitle = "从之前创建的 .obackup 备份文件恢复全部数据。当前数据将被覆盖。",
    ) {
        OutlinedButton(
            onClick = onRestoreClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("restore_btn"),
            border = BorderStroke(1.dp, defaults.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = defaults.pageForeground,
            ),
        ) {
            Text("从备份恢复")
        }
    }
}

/**
 * Section for exporting current month as CSV.
 * This is a share/export convenience feature, NOT a full backup.
 */
@Composable
internal fun ExportDataSection(
    onExportDataClick: () -> Unit,
) {
    val defaults = OvertimeTheme.defaults

    SettingCard(
        title = "导出本月数据",
        subtitle = "将当前月的加班与调休明细导出为 CSV 表格，并通过系统分享发送。注意：此方式导出的 CSV 不是完整备份，恢复请使用「备份/恢复」功能。",
    ) {
        Button(
            onClick = onExportDataClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("export_csv_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = defaults.accent,
                contentColor = defaults.accentOn,
            ),
        ) {
            Text("导出本月 CSV 数据")
        }
    }
}
