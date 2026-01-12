package com.smartethnet.lib.packet

/**
 * 报文类型
 */
enum class RustunPacketType(val value: Byte) {
    HAND_SHAKE(1),
    KEEP_ALIVE(2),
    DATA(3),
    HAND_SHAKE_REPLY(4),
    PROBE_IPV6(6),
    PROBE_HOLE_PUNCH(7);

    companion object {
        private val valueMap = entries.associateBy { it.value }

        fun fromValue(value: Byte): RustunPacketType? = valueMap[value]
    }
}