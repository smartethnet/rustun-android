package com.smartethnet.lib

import com.smartethnet.lib.crypto.RustunCrypto
import com.smartethnet.lib.packet.RustunPacket
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * 自定义 TCP 报文
 * 报文协议如下
 * | ---- magic (4 bytes) ---- | ---- version (1 byte) ---- | ---- type (1 byte) ---- | ---- payload length (2 bytes) ---- | ---- data (n bytes) ---- |
 */
class RustunPacketEncoder(val crypto: RustunCrypto) : MessageToByteEncoder<RustunPacket>() {

    override fun encode(ctx: ChannelHandlerContext, msg: RustunPacket, out: ByteBuf) {
        // 写入魔数 (4 bytes)
        out.writeInt(msg.magic)

        // 写入版本号 (1 byte)
        out.writeByte(msg.version.toInt())

        // 写入消息类型 (1 byte)
        out.writeByte(msg.type.toInt())

        // 写入数据长度 (2 bytes)
        out.writeShort(msg.length)

        // 发送数据
        if (msg.data.isNotEmpty()) {
            // 执行加密
            val encodedMsg = crypto.encrypt(msg.data)

            // 发送
            out.writeBytes(encodedMsg)
        }
    }
}