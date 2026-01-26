package com.smartethnet.rustun.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartethnet.rustun.proto.Config
import com.smartethnet.rustun.service.RustunVpnService
import com.smartethnet.rustun.util.ConnectState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ConnectViewModel(val appContext: Context) : ViewModel() {

    init {
        require(appContext.applicationContext is Application) {
            "Context must be Application context"
        }
    }

    private companion object {
        const val TAG = "ConnectViewModel"
        private const val TIME_UPDATE_INTERVAL = 1000L
    }

    var error by mutableStateOf<String?>(null)
        private set

    // connection config
    private var _config by mutableStateOf<Config?>(null)
    private var vpnControl by mutableStateOf<RustunVpnService.RustunVpnServiceBinder?>(null)
    private var isServiceBound by mutableStateOf(false)

    // vpn服务状态
    val state: StateFlow<ConnectState> = RustunVpnService.serviceState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ConnectState.DISCONNECTED
    )

    // 在线时间
    var onlineTime by mutableStateOf("-")
        private set

    private var timeUpdateJob: Job? = null

    private fun startTimeUpdate() {
        timeUpdateJob?.cancel()
        timeUpdateJob = viewModelScope.launch {
            while (isActive) {
                updateOnlineTime()
                delay(TIME_UPDATE_INTERVAL)
            }
        }
    }

    private fun stopTimeUpdate() {
        timeUpdateJob?.cancel()
        timeUpdateJob = null
        onlineTime = "-"
    }

    @SuppressLint("DefaultLocale")
    private fun updateOnlineTime() {
        val startTime = vpnControl?.getService()?.startTime ?: -1L
        if (startTime > 0) {
            val elapsedMillis = System.currentTimeMillis() - startTime
            val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60

            onlineTime = String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            onlineTime = "-"
        }
    }

    // 与vpn服务的链接
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (service is RustunVpnService.RustunVpnServiceBinder) {
                vpnControl = service
                isServiceBound = true

                // 绑定成功后，马上启动VPN服务
                _config?.let { config ->
                    startVpnService(service, config)
                }
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            vpnControl = null
            isServiceBound = false
            stopTimeUpdate()
        }
    }

    fun start(config: Config) = viewModelScope.launch {
        try {
            error = null

            // 如果正在连接或已连接，先停止
            if (state.value != ConnectState.DISCONNECTED) {
                stop()
                // 等待服务停止
                delay(500)
            }

            _config = config

            if (isServiceBound && vpnControl != null) {
                startVpnService(vpnControl!!, config)
            } else {
                // 绑定服务
                val intent = Intent(appContext, RustunVpnService::class.java)
                val bound = appContext.bindService(
                    intent,
                    serviceConnection,
                    Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
                )

                if (!bound) {
                    error = "无法绑定VPN服务"
                    Log.e(TAG, "Failed to bind VPN service")
                }
            }
        } catch (e: Throwable) {
            error = "启动失败: ${e.localizedMessage}"
            Log.e(TAG, "Failed to start VPN", e)
        }
    }

    fun startVpnService(service: RustunVpnService.RustunVpnServiceBinder, config: Config) =
        viewModelScope.launch {
            try {
                service.getService().start(config)
                startTimeUpdate()
            } catch (e: Throwable) {
                error = "启动失败: ${e.localizedMessage}"
                Log.e(TAG, "Failed to start VPN service", e)
            }
        }

    fun stop() = viewModelScope.launch {
        try {
            vpnControl?.getService()?.stop()
            stopTimeUpdate()
            _config = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop VPN", e)
        }
    }

    fun disconnectService() {
        if (isServiceBound) {
            try {
                appContext.unbindService(serviceConnection)
            } catch (_: IllegalArgumentException) {
                // 服务可能已经解绑，忽略这个异常
                Log.w(TAG, "Service already unbound")
            }
            isServiceBound = false
            vpnControl = null
        }
        stopTimeUpdate()
    }

    override fun onCleared() {
        super.onCleared()
        disconnectService()
        timeUpdateJob?.cancel()
    }
}