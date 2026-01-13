package com.smartethnet.lib.packet

import com.smartethnet.lib.RustunClientHandler.Companion.gson
import com.smartethnet.lib.message.HandshakeMessage
import com.smartethnet.lib.message.KeepAliveMessage

/**
 * 自定义 TCP 报文
 * 报文协议如下
 * | ---- magic (4 bytes) ---- | ---- version (1 byte) ---- | ---- type (1 byte) ---- | ---- payload length (2 bytes) ---- | ---- data (n bytes) ---- |
 */
data class RustunPacket(var type: Byte, var length: Int, var data: ByteArray) {
    val magic: Int = MAGIC_NUMBER
    val version: Byte = VERSION

    companion object {
        const val MAGIC_NUMBER: Int = 0x91929394.toInt()
        const val VERSION: Byte = 0x01

        fun handShakePacket(identity: String): RustunPacket {
            val message = HandshakeMessage(identity)
            val data = gson.toJson(message).toByteArray()
            return RustunPacket(RustunPacketType.HAND_SHAKE.value, data.size, data)
        }

        fun heartbeatPacket(identity: String): RustunPacket {
            val message = KeepAliveMessage(identity, "", 0, "", 0, arrayOf())
            val data = gson.toJson(message).toByteArray()
            return RustunPacket(RustunPacketType.KEEP_ALIVE.value, data.size, data)
        }
        
        fun dataPacket(data: ByteArray): RustunPacket {
            return RustunPacket(RustunPacketType.DATA.value, data.size, data)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RustunPacket

        if (type != other.type) return false
        if (length != other.length) return false
        if (magic != other.magic) return false
        if (version != other.version) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.toInt()
        result = 31 * result + length
        result = 31 * result + magic
        result = 31 * result + version
        result = 31 * result + data.contentHashCode()
        return result
    }
}