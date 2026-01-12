package com.smartethnet.rustun.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smartethnet.lib.RustunClient
import com.smartethnet.lib.RustunEventListener
import com.smartethnet.lib.message.DataMessage
import com.smartethnet.lib.message.HandShakeReplyMessage
import com.smartethnet.rustun.R
import com.smartethnet.rustun.proto.Config
import com.smartethnet.rustun.util.ConnectState
import com.smartethnet.rustun.util.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

@SuppressLint("VpnServicePolicy")
class RustunVpnService : VpnService(), RustunEventListener {
    private val channelId = "Rustun"
    private val scope = CoroutineScope(Dispatchers.IO)
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var output: FileOutputStream? = null
    private var vpnDataReaderJob: Job? = null
    private val binder = RustunVpnServiceBinder()

    private var client: RustunClient? = null

    companion object {
        const val TAG = "Rustun Vpn Service"
        private val _serviceState = MutableStateFlow<ConnectState>(ConnectState.DISCONNECTED)
        val serviceState = _serviceState.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()

        // 创建通知渠道（Android 8.0+ 必需）
        val channel = NotificationChannel(
            channelId, "rustun vpn service", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun showForegroundNotification() {
        // 构建通知消息
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Smart ethnet 服务运行中")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

    suspend fun start(config: Config): String? {
        // 更新状态
        _serviceState.value = ConnectState.CONNECTING

        // 连接服务器
        try {
            withContext(scope.coroutineContext) {
                // 解析服务器地址
                val (serverIp, serverPort) = parseServerAddress(config.server)

                // 发起连接
                client = RustunClient(
                    serverIp,
                    serverPort,
                    config.identity,
                    config.crypto,
                    config.secret,
                    this@RustunVpnService
                )

                // 输出配置信息
                logConnectionInfo(serverIp, serverPort, config.identity, config.crypto)

                // 开始连接
                client?.start()
            }
        } catch (e: Throwable) {
            output?.close()
            parcelFileDescriptor?.close()

            // 更新状态
            _serviceState.value = ConnectState.DISCONNECTED
            return e.toString()
        }

        return null
    }

    /**
     * Network Interface -> server
     */
    fun transferVpnData() = scope.launch {
        val input = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val buffer = ByteArray(8192)
        try {
            while (isActive) {
                try {
                    val length = input.read(buffer)
                    if (length <= 0) continue

                    // 读取data
                    val data = buffer.sliceArray(IntRange(0, length - 1))

                    // 解析报文
                    val pair = parseVpnPacket(data)
                    if (pair != null) {
                        Log.d(TAG, "${pair.first} -> ${pair.second}: $length bytes")

                        // 转发
                        client?.write(data)
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "转发[VPN网卡]数据到失败", e)
                }
            }
        } finally {
            input.close()
        }
    }

    fun stop() = scope.launch {
        vpnDataReaderJob?.cancel()
        client?.stop()
        output?.close()
        parcelFileDescriptor?.close()

        // 更新状态
        _serviceState.value = ConnectState.DISCONNECTED

        stopSelf()
    }

    override fun onConnected() {
        Log.i(TAG, "connect server success")
    }

    override fun onDisconnected() {
        // 停止服务
        stop()
        Log.i(TAG, "disconnect from server")
    }

    /**
     * server to Network interface
     */
    override fun onDataMessage(message: DataMessage) {
        val data = message.payload
        output?.write(data)
    }

    override fun onHandShakeReplayMessage(message: HandShakeReplyMessage) {
        Log.d(TAG, "onHandShakeReplayMessage $message")

        // 设置VPN
        val builder = Builder()

        // 设置路由
        val gateway = message.gateway
        Log.i(TAG, "gateway is $gateway")

        val prefix = subnetMaskToPrefixLength(message.mask)
        Log.i(TAG, "translate network mask ${message.mask} to $prefix")

        val subnet = NetworkUtil.calculateNetworkSegment(gateway, message.mask)
        builder.addRoute(subnet, prefix)
        Log.i(TAG, "add route $subnet/$prefix")

        message.peerDetails.forEach { peer ->
            peer.ciders.forEach {
                val (ip, mask) = parseCidr(it)
                builder.addRoute(ip, mask)
                Log.i(TAG, "add route $ip/$mask")
            }
        }

        // 设置DNS
        builder.addDnsServer("8.8.8.8")
        Log.i(TAG, "add dns server 8.8.8.8")
        builder.addDnsServer("8.8.4.4")
        Log.i(TAG, "add dns server 8.8.4.4")

        // 设置地址
        builder.addAddress(message.privateIp, prefix)
        Log.i(TAG, "add address ${message.privateIp}")

        // 建立连接
        parcelFileDescriptor = builder.establish()
        output = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

        // 开启数据转发(从tunnel网卡读取数据，转发给服务器)
        vpnDataReaderJob = transferVpnData()

        // 更新状态
        _serviceState.value = ConnectState.CONNECTED

        // 显示前台服务通知
        showForegroundNotification()
        Log.i(TAG, "Rustun vpn server started")
    }

    fun subnetMaskToPrefixLength(subnetMask: String): Int {
        val bytes = subnetMask.split(".")
            .map { it.toInt().toByte() }
            .toByteArray()

        var prefixLength = 0
        for (byte in bytes) {
            // Byte转Int时需要处理符号位
            val unsignedByte = byte.toInt() and 0xFF
            prefixLength += if (unsignedByte == 255) {
                8
            } else if (unsignedByte == 0) {
                break
            } else {
                Integer.bitCount(unsignedByte)
            }
        }
        return prefixLength
    }

    /**
     * 解析服务器地址
     * @param serverUrl 服务器地址，格式: "ip:port"
     * @return Pair<服务器IP, 端口号>
     */
    private fun parseServerAddress(serverUrl: String): Pair<String, Int> {
        return if (":" in serverUrl) {
            val array = serverUrl.split(":")
            val ip = array[0]
            val port = array.getOrNull(1)?.toIntOrNull()
                ?: throw IllegalArgumentException("invalid server url：$serverUrl")
            ip to port
        } else {
            throw IllegalArgumentException("invalid server url：$serverUrl")
        }
    }

    private fun parseCidr(cidr: String): Pair<String, Int> {
        return if ("/" in cidr) {
            val array = cidr.split("/")
            val ip = array[0]
            val prefix = array.getOrNull(1)?.toIntOrNull()
                ?: throw IllegalArgumentException("invalid route: $cidr")
            ip to prefix
        } else {
            throw IllegalArgumentException("invalid route: $cidr")
        }
    }

    fun parseVpnPacket(packet: ByteArray): Pair<String, String>? {
        if (packet.size < 20) {
            return null
        }

        // 解析IPv4版本
        val version = (packet[0].toInt() shr 4) and 0x0F // 版本字段是第一个字节的高4位
        if (version != 4) {
            return null
        }

        // 获取源IP和目的IP地址（IPv4头部从第12位到第19位）
        val sourceIp = String.format(
            Locale.CHINA,
            "%d.%d.%d.%d",
            packet[12].toInt() and 0xFF,
            packet[13].toInt() and 0xFF,
            packet[14].toInt() and 0xFF,
            packet[15].toInt() and 0xFF
        )
        val destinationIp = String.format(
            Locale.CHINA,
            "%d.%d.%d.%d",
            packet[16].toInt() and 0xFF,
            packet[17].toInt() and 0xFF,
            packet[18].toInt() and 0xFF,
            packet[19].toInt() and 0xFF
        )

        // 返回结果
        return Pair(sourceIp, destinationIp)
    }

    private fun logConnectionInfo(
        serverIp: String,
        serverPort: Int,
        identity: String,
        crypto: String
    ) {
        Log.i(TAG, "==================================")
        Log.i(TAG, "Server IP  : $serverIp")
        Log.i(TAG, "Server Port: $serverPort")
        Log.i(TAG, "Client     : $identity")
        Log.i(TAG, "Crypt      : $crypto")
        Log.i(TAG, "==================================")
    }

    inner class RustunVpnServiceBinder : Binder() {
        fun getService(): RustunVpnService {
            return this@RustunVpnService
        }
    }
}