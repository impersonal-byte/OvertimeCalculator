package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.UpdateUiState
import com.peter.overtimecalculator.ui.AppUpdateUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsTopBar(
    title: String,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button"),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
    )
}

@Composable
internal fun SettingCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
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

@Composable
internal fun MultiplierBadge(label: String, value: String) {
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

internal fun aboutStatusSubtitle(updateUiState: AppUpdateUiState): String {
    val versionLabel = if (updateUiState.currentVersionName.startsWith("v")) {
        updateUiState.currentVersionName
    } else {
        "v${updateUiState.currentVersionName}"
    }

    val statusLabel = when {
        updateUiState.awaitingInstallPermission -> "等待安装授权"
        updateUiState.updateState is UpdateUiState.Checking -> "正在检查更新"
        updateUiState.updateState is UpdateUiState.Downloading -> "正在下载更新"
        updateUiState.updateState is UpdateUiState.UpdateAvailable -> "发现新版本"
        updateUiState.updateState is UpdateUiState.ReadyToInstall -> "准备安装更新"
        updateUiState.updateState is UpdateUiState.UpToDate -> "已是最新版本"
        updateUiState.updateState is UpdateUiState.Error -> "更新状态异常"
        else -> "可检查更新"
    }

    return "$versionLabel · $statusLabel"
}

internal fun updateStatusLabel(updateState: UpdateUiState, awaitingInstallPermission: Boolean): String {
    return when {
        awaitingInstallPermission -> "请在系统设置中允许安装未知应用，返回后会继续安装。"
        updateState is UpdateUiState.Checking -> "正在检查 GitHub 最新版本…"
        updateState is UpdateUiState.Downloading -> {
            val progress = updateState.progressPercent
            if (progress == null) {
                "正在下载 ${updateState.remoteVersion}…"
            } else {
                "正在下载 ${updateState.remoteVersion}…$progress%"
            }
        }
        updateState is UpdateUiState.UpToDate -> "当前已是最新版本。"
        updateState is UpdateUiState.UpdateAvailable -> "发现新版本 ${updateState.remoteVersion}。"
        updateState is UpdateUiState.ReadyToInstall -> "更新包已准备完成，正在拉起安装。"
        updateState is UpdateUiState.Error -> updateState.message
        else -> "点击后会检查 GitHub 最新正式版本，并直接下载更新包。"
    }
}
