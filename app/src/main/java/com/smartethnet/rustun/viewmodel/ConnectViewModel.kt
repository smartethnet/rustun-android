package com.smartethnet.rustun.viewmodel

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConnectViewModel(val context: Context) : ViewModel() {

    private companion object {
        const val TAG = "ConnectViewModel"
    }

    var error by mutableStateOf<String?>(null)
        private set

    // connection config
    private var _config by mutableStateOf<Config?>(null)

    // vpn controller
    var vpnControl by mutableStateOf<RustunVpnService.RustunVpnServiceBinder?>(null)

    // vpn服务状态
    val state: StateFlow<ConnectState> = RustunVpnService.serviceState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ConnectState.DISCONNECTED
    )

    // 在线时间
    var onlineTime by mutableStateOf("-")
        private set
    private val job = viewModelScope.launch {
        while (isActive) {
            if (vpnControl != null) {
                val now = System.currentTimeMillis()
                val startTime = vpnControl!!.getService().startTime

                if (startTime > 0) {
                    val elapse = now - startTime

                    // calc hour and second
                    val hour = (elapse / 1000) / 3600
                    val second = (elapse / 1000) % 60

                    // update online time
                    onlineTime = "$hour:$second"
                } else {
                    // update online time
                    onlineTime = "-"
                }
            }
            
            delay(1000)
        }
    }

    // 与vpn服务的链接
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (service is RustunVpnService.RustunVpnServiceBinder && _config != null) {
                vpnControl = service

                // 绑定成功后，马上启动VPN服务
                startVpnService(service, _config!!)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            vpnControl = null
        }
    }

    fun start(config: Config) {
        _config = config

        if (vpnControl != null) {
            startVpnService(vpnControl!!, config)
        } else {
            // 启动服务
            val intent = Intent(context, RustunVpnService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.i(TAG, "starting rustun vpn service")
        }
    }

    fun startVpnService(service: RustunVpnService.RustunVpnServiceBinder, config: Config) =
        viewModelScope.launch {
            service.getService().start(config)
        }

    fun stop() = viewModelScope.launch {
        vpnControl?.getService()?.stop()
        _config = null
    }

    override fun onCleared() {
        super.onCleared()
        stop()

        try {
            context.unbindService(serviceConnection)
        } catch (_: Throwable) {
        }

        job.cancel()
    }
}