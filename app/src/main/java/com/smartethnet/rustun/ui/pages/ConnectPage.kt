package com.smartethnet.rustun.ui.pages

import android.app.Activity.RESULT_OK
import android.net.VpnService
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.smartethnet.rustun.R
import com.smartethnet.rustun.proto.Config
import com.smartethnet.rustun.ui.component.ErrorMessageDialog
import com.smartethnet.rustun.util.ConnectState
import com.smartethnet.rustun.viewmodel.ConnectViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConnectPage(
    config: Config,
    viewModel: ConnectViewModel,
    onBack: () -> Unit = {}
) {
    val TAG = "Rustun"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 错误消息
    val error = viewModel.error

    // 弹窗错误
    var dialogError by remember { mutableStateOf<String?>(null) }
    ErrorMessageDialog(dialogError, onDismiss = { dialogError = null })

    // 申请VPN权限
    val vpnLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            scope.launch {
                if (result.resultCode == RESULT_OK) {
                    Log.i(TAG, "vpn permission granted")
                    viewModel.start(config)
                } else {
                    Log.e(TAG, "failed to get vpn permission")
                    dialogError = "VPN权限申请失败！请授予VPN权限，以便应用正常运行。"
                }
            }
        }

    // 处理连接事件
    val handleConnect = fun() {
        // 申请权限
        val intent = VpnService.prepare(context)

        // intent不等于空，说明没有授予权限，需要申请权限
        if (intent != null) {
            Log.i(TAG, "asking for vpn permission")
            vpnLauncher.launch(intent)
        }
        // 已有VPN权限，启动vpn服务
        else {
            viewModel.start(config)
        }
    }

    // 处理断开连接事件
    val handleDisconnect = fun() {
        viewModel.stop()
    }

    // vpn服务状态
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = { Text(config.name) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding() + 24.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    start = 24.dp,
                    end = 24.dp
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ControlPanel(config, state, handleConnect, handleDisconnect, error)
        }
    }
}

@Composable
fun ControlPanel(
    config: Config,
    state: ConnectState,
    handleConnect: () -> Unit = {},
    handleDisconnect: () -> Unit = {},
    error: String? = null
) {
    val checked = when (state) {
        ConnectState.CONNECTED -> true
        ConnectState.CONNECTING -> true
        else -> false
    }
    val stateText = when (state) {
        ConnectState.CONNECTED -> "已连接"
        ConnectState.CONNECTING -> "连接中"
        ConnectState.DISCONNECTED -> "已断开"
    }
    val errorTextColor = if (error != null) {
        MaterialTheme.colorScheme.error
    } else if (ConnectState.CONNECTED == state) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Unspecified
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = if (error != null) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.outlinedCardBorder()
        },
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("服务器信息", style = MaterialTheme.typography.titleLarge)
                Switch(
                    checked,
                    onCheckedChange = {
                        if (checked) {
                            handleDisconnect()
                        } else {
                            handleConnect()
                        }
                    },
                    thumbContent = {
                        if (checked) {
                            if (state == ConnectState.CONNECTING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }

                        }
                    },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_cloud_24),
                    contentDescription = null
                )
                Text(config.server)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_devices_24),
                    contentDescription = null
                )
                Text(config.identity)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    Text(text = stateText, color = errorTextColor)
                }

                Text("00:00")
            }

            if (error != null) Text(text = error, color = errorTextColor)
        }
    }
}
