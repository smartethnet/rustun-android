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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
    }
}