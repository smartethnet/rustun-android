package com.smartethnet.lib

import android.util.Log
import com.google.gson.Gson
import com.smartethnet.lib.message.DataMessage
import com.smartethnet.lib.message.HandShakeReplyMessage
import com.smartethnet.lib.message.KeepAliveMessage
import com.smartethnet.lib.message.ProbeHolePunchMessage
import com.smartethnet.lib.message.ProbeIpv6Message
import com.smartethnet.lib.packet.RustunPacket
import com.smartethnet.lib.packet.RustunPacketType
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class RustunClientHandler(val identity: String, val listener: RustunEventListener) :
    SimpleChannelInboundHandler<RustunPacket>() {
    companion object {
        const val TAG = "Rustun Client"
        val gson = Gson()
    }

    override fun messageReceived(ctx: ChannelHandlerContext, packet: RustunPacket) {
        when (packet.type) {
            RustunPacketType.HAND_SHAKE_REPLY.value -> {
                val value = String(packet.data)
                val message = gson.fromJson(value, HandShakeReplyMessage::class.java)
                listener.onHandShakeReplayMessage(message)
            }

            RustunPacketType.KEEP_ALIVE.value -> {
                val value = String(packet.data)
                val message = gson.fromJson(value, KeepAliveMessage::class.java)
                listener.onKeepAliveMessage(message)
            }

            RustunPacketType.PROBE_IPV6.value -> {
                val value = String(packet.data)
                val message = gson.fromJson(value, ProbeIpv6Message::class.java)
                listener.onProbeIpv6Message(message)
            }

            RustunPacketType.PROBE_HOLE_PUNCH.value -> {
                val value = String(packet.data)
                val message = gson.fromJson(value, ProbeHolePunchMessage::class.java)
                listener.onProbeHolePunchMessage(message)
            }

            RustunPacketType.DATA.value -> {
                val message = DataMessage(packet.data)
                listener.onDataMessage(message)
            }

            else -> {
                Log.d(TAG, "unhandled packet: $packet")
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        listener.onError(cause)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        // 发送握手消息
        val packet = RustunPacket.handShakePacket(identity)
        Log.d(TAG, "send hand shake message, identity = $identity")
        ctx.channel().writeAndFlush(packet)

        // 触发事件
        listener.onConnected()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        listener.onDisconnected()
    }
}