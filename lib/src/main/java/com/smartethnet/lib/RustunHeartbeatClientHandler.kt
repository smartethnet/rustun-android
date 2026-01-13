package com.smartethnet.lib

import android.util.Log
import com.smartethnet.lib.packet.RustunPacket
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleStateEvent

class RustunHeartbeatClientHandler(val identity: String) : ChannelHandlerAdapter() {
    companion object {
        const val TAG = "Rustun Heartbeat"
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            // 发送心跳包
            val packet = RustunPacket.heartbeatPacket(identity)
            ctx.channel().writeAndFlush(packet)

            Log.d(TAG, "sent keep alive message")
        }
    }
}