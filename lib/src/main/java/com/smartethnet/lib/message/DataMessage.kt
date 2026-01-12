package com.smartethnet.lib.message

data class DataMessage(val payload: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataMessage

        return payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        return payload.contentHashCode()
    }
}