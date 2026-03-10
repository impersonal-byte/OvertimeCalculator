package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.UpdateUiState
import com.peter.overtimecalculator.ui.AppUpdateUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    updateUiState: AppUpdateUiState,
    onCheckForUpdates: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            SettingsTopBar(
                title = "关于加薪",
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                SettingCard("检查更新", "连接 GitHub 获取最新正式版本。") {
                    Text(
                        text = "当前内部版本：${updateUiState.currentVersionName}",
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
                SettingCard("节假日更新与判定", "本地基线与远端拉取结合。") {
                    Text(
                        "应用内置节假日基线数据，并会通过 Timor 中国节假日 API 静默刷新当前年和下一年规则；当远端无有效数据时，会自动回退到内置基线与周末规则。任何一天都可以在首页录入时手动覆盖类型。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
