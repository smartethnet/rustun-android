package com.smartethnet.lib

import com.smartethnet.lib.crypto.RustunCrypto
import com.smartethnet.lib.packet.RustunPacket
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.CorruptedFrameException

/**
 * 自定义 TCP 报文
 * 报文协议如下
 * | ---- magic (4 bytes) ---- | ---- version (1 byte) ---- | ---- type (1 byte) ---- | ---- payload length (2 bytes) ---- | ---- data (n bytes) ---- |
 */
class RustunPacketDecoder(val crypto: RustunCrypto) : ByteToMessageDecoder() {
    // 协议头固定长度：4(魔数) + 1(版本) + 1(类型) + 2(长度) = 8 bytes
    private val headerLength = 8
    private val maxLength = 65535
    private var closed = false

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        // 判断是否已关闭
        if (closed) return

        // 确保有足够的数据读取协议头
        if (input.readableBytes() < headerLength) {
            return
        }

        // 标记当前读取位置，如果数据不完整可以回退
        input.markReaderIndex()

        // 读取魔数 (4 bytes)
        val magic = input.readUnsignedInt()

        // 验证魔数
        if (magic.toInt() != RustunPacket.MAGIC_NUMBER) {
            closed = true
            ctx.close()
            throw CorruptedFrameException("Invalid magic number: 0x${magic.toString(16)}")
        }

        // 读取版本号 (1 byte)
        val version = input.readUnsignedByte()

        // 验证版本号
        if (version != RustunPacket.VERSION.toShort()) {
            closed = true
            ctx.close()
            throw CorruptedFrameException("Invalid version: 0x${version.toString(16)}")
        }

        // 读取消息类型 (1 byte)
        val type = input.readByte()

        // 读取数据长度 (2 bytes, 无符号)
        val length = input.readUnsignedShort()

        // 检查最大长度限制，防止恶意攻击
        if (length > maxLength) {
            closed = true
            ctx.close()
            throw CorruptedFrameException("Data length too large: $length (max: $maxLength)")
        }

        // 检查是否有足够的数据体
        if (input.readableBytes() < length) {
            // 数据不完整，回退读取位置，等待更多数据
            input.resetReaderIndex()
            return
        }

        // 读取数据部分
        var data = if (length > 0) {
            val dataBytes = ByteArray(length)
            input.readBytes(dataBytes)
            dataBytes
        } else {
            ByteArray(0)
        }

        // 解密数据
        data = crypto.decrypt(data)

        // 创建协议消息对象
        val message = RustunPacket(
            type = type,
            length = length,
            data = data
        )

        // 添加到输出列表，传递给下一个处理器
        out.add(message)
    }
}