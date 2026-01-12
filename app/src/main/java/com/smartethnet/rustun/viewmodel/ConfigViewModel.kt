package com.smartethnet.rustun.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartethnet.rustun.proto.Config
import com.smartethnet.rustun.proto.ConfigList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ConfigViewModel(
    val configListDataStore: DataStore<ConfigList>
) : ViewModel() {

    // UI状态
    var uiState by mutableStateOf(UiState())
        private set

    // 保存结果
    private val _onSuccess = MutableSharedFlow<Boolean>()
    val onSuccess = _onSuccess.asSharedFlow()

    fun onNameChange(value: String) {
        uiState = uiState.copy(
            name = value,
            nameError = validateName(value)
        )
    }

    fun validateName(value: String): String? {
        return when {
            value.isEmpty() -> "请填写配置名称"
            else -> null
        }
    }

    fun onServerUrlChange(value: String) {
        uiState = uiState.copy(
            server = value,
            serverError = validateServerUrl(value)
        )
    }

    fun validateServerUrl(value: String): String? {
        return when {
            value.isEmpty() -> "请填写服务器地址"
            else -> null
        }
    }

    fun onServerEncryptAlgChange(value: String) {
        uiState = uiState.copy(
            crypto = value
        )
    }

    fun onServerSecretChange(value: String) {
        uiState = uiState.copy(
            secret = value,
            secretError = validateServerSecret(value)
        )
    }

    fun validateServerSecret(value: String): String? {
        return when {
            value.isEmpty() -> "请填写密钥"
            else -> null
        }
    }

    fun onClientIdChange(value: String) {
        uiState = uiState.copy(
            identity = value,
            identityError = validateClientId(value)
        )
    }

    fun validateClientId(value: String): String? {
        return when {
            value.isEmpty() -> "请填写客户端标识"
            else -> null
        }
    }

    fun saveSettings() {
        // 先校验字段
        val nameError = validateName(uiState.name)
        val serverUrlError = validateServerUrl(uiState.server)
        val serverSecretError = validateServerSecret(uiState.secret)
        val clientIdError = validateClientId(uiState.identity)

        // 如果有错误，更新状态
        if (nameError != null || serverUrlError != null || serverSecretError != null || clientIdError != null) {
            uiState = uiState.copy(
                nameError = nameError,
                serverError = serverUrlError,
                secretError = serverSecretError,
                identityError = clientIdError,
                submitting = false
            )

            return
        }

        // 开始提交
        uiState = uiState.copy(submitting = true)
        viewModelScope.launch {
            val config = Config.newBuilder()
                .setName(uiState.name)
                .setServer(uiState.server)
                .setSecret(uiState.secret)
                .setIdentity(uiState.identity)
                .setCrypto(uiState.crypto)
                .build()

            configListDataStore.updateData { old ->
                val list = ConfigList.newBuilder()
                    .addAllConfig(old.configList)
                    .addConfig(config)
                    .build()
                return@updateData list
            }

            _onSuccess.emit(true)

            // 更新状态
            uiState = uiState.copy(submitting = false)
        }
    }

    data class UiState(
        val name: String = "",
        val nameError: String? = null,
        val server: String = "",
        val serverError: String? = null,
        val crypto: String = "ChaCha20-Poly1305",
        val secret: String = "",
        val secretError: String? = null,
        val identity: String = "",
        val identityError: String? = null,

        val submitting: Boolean = false
    )
}