package com.smartethnet.lib

import android.util.Log
import com.google.gson.Gson
import com.smartethnet.lib.message.KeepAliveMessage
import com.smartethnet.lib.packet.RustunPacket
import com.smartethnet.lib.packet.RustunPacketType
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleStateEvent

class RustunHeartbeatClientHandler(val identity: String) : ChannelHandlerAdapter() {
    companion object {
        const val TAG = "Rustun Heartbeat"
        val gson = Gson()
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            // 发送心跳包
            val message = KeepAliveMessage(identity, "", 0, "", 0, arrayOf())
            val data = gson.toJson(message).toByteArray()
            val packet = RustunPacket(RustunPacketType.KEEP_ALIVE.value, data.size, data)
            ctx.channel().writeAndFlush(packet)

            Log.d(TAG, "send keep alive message: ${gson.toJson(message)}")
        }
    }
}