package com.smartethnet.rustun.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartethnet.rustun.ui.component.InputField
import com.smartethnet.rustun.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigEditPage(
    viewModel: ConfigViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState = viewModel.uiState

    // 处理保存结果
    LaunchedEffect(Unit) {
        viewModel.onSuccess.collect { _ ->
            onBack()
        }
    }

    // UI
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("取消") }
                },
                actions = {
                    TextButton(onClick = viewModel::saveSettings) { Text("保存") }
                },
                title = { Text("新建配置") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 配置名称
                InputField(
                    value = uiState.name,
                    error = uiState.nameError,
                    label = "配置名称",
                    placeholder = "请输入配置名称",
                    onValueChange = viewModel::onNameChange
                )

                // 服务端地址
                InputField(
                    value = uiState.server,
                    error = uiState.serverError,
                    label = "服务器",
                    placeholder = "请输入服务器地址 IP:PORT",
                    onValueChange = viewModel::onServerUrlChange
                )

                // 加密方式
                EncryptAlgList(
                    uiState.crypto,
                    viewModel::onServerEncryptAlgChange,
                    label = "加密",
                    placeholder = "请选择加密方式",
                )

                // 密钥
                InputField(
                    value = uiState.secret,
                    error = uiState.secretError,
                    label = "密钥",
                    placeholder = "请输入密钥",
                    onValueChange = viewModel::onServerSecretChange
                )

                // 客户端标识
                InputField(
                    value = uiState.identity,
                    error = uiState.identityError,
                    label = "客户端标识",
                    placeholder = "请输入客户端标识",
                    onValueChange = viewModel::onClientIdChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptAlgList(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
) {
    val isError = when {
        !error.isNullOrBlank() -> true
        else -> false
    }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = true,
            singleLine = true,
            isError = isError,
            shape = MaterialTheme.shapes.small,
            label = { Text(label ?: "") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraSmall)
                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
            placeholder = {
                Text(
                    text = placeholder ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            supportingText = { Text(text = error ?: "") },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("ChaCha20-Poly1305") },
                onClick = { expanded = false; onValueChange("ChaCha20-Poly1305") })
            DropdownMenuItem(
                text = { Text("AES-256-GCM") },
                onClick = { expanded = false; onValueChange("AES-256-GCM") })
            DropdownMenuItem(
                text = { Text("XOR") },
                onClick = { expanded = false; onValueChange("XOR") })
            DropdownMenuItem(
                text = { Text("Plain") },
                onClick = { expanded = false; onValueChange("Plain") })
        }
    }
}