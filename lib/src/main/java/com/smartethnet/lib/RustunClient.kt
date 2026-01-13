package com.smartethnet.lib

import com.smartethnet.lib.crypto.RustunCrypto
import com.smartethnet.lib.crypto.RustunPlainCrypto
import com.smartethnet.lib.crypto.RustunXorCrypto
import com.smartethnet.lib.packet.RustunPacket
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

class RustunClient(
    val ip: String,
    val port: Int,
    val identity: String,
    val crypto: String,
    val secret: String,
    val listener: RustunEventListener
) {
    var group: NioEventLoopGroup? = null
    var channel: Channel? = null

    private fun buildCrypto(crypto: String, secret: String): RustunCrypto {
        return when (crypto) {
            "XOR" -> RustunXorCrypto(secret)
            else -> RustunPlainCrypto()
        }
    }

    fun start() {
        // 配置加密器
        val crypto = buildCrypto(crypto, secret)

        // 配置boostrap
        group = NioEventLoopGroup()
        val bootstrap = Bootstrap()
        bootstrap.group(group)
            // 使用异步
            .channel(NioSocketChannel::class.java)
            // 配置服务器地址
            .remoteAddress(ip, port)
            // 设置连接超时时间（3秒）
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            // 启用TCP keep alive
            .option(ChannelOption.SO_KEEPALIVE, true)
            // 启用TCP no delay
            .option(ChannelOption.TCP_NODELAY, true)
            // 配置Handler
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().apply {
                        // 添加编码解码器
                        addLast(RustunPacketEncoder(crypto))
                        addLast(RustunPacketDecoder(crypto))

                        // 添加心跳检测
                        addLast(IdleStateHandler(0, 10, 0, TimeUnit.SECONDS))
                        addLast(RustunHeartbeatClientHandler(identity))

                        // 添加客户端消息处理
                        addLast(RustunClientHandler(identity, listener))
                    }
                }
            })

        try {
            // 发起连接（同步）
            val future = bootstrap.connect().sync()
            channel = future.channel()
        } catch (_: Throwable) {
            group?.shutdownGracefully()
        }
    }

    fun stop() {
        channel?.close()?.sync()
        channel = null

        group?.shutdownGracefully()
        group = null
    }

    fun write(data: ByteArray) {
        // 发送数据包
        val packet = RustunPacket.dataPacket(data)
        channel?.writeAndFlush(packet)
    }
}