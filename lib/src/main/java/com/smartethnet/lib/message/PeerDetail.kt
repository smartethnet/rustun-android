package com.smartethnet.lib.message

import com.google.gson.annotations.SerializedName

data class PeerDetail(
    @SerializedName("identity")
    val identity: String,

    @SerializedName("private_ip")
    val privateIp: String,

    @SerializedName("ciders")
    val ciders: Array<String>,

    @SerializedName("ipv6")
    val ipv6: String,

    @SerializedName("port")
    val port: Int,

    @SerializedName("stun_ip")
    val stunIp: String,

    @SerializedName("stun_port")
    val stunPort: Int,

    @SerializedName("last_active")
    val lastActive: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PeerDetail

        if (port != other.port) return false
        if (stunPort != other.stunPort) return false
        if (lastActive != other.lastActive) return false
        if (identity != other.identity) return false
        if (privateIp != other.privateIp) return false
        if (!ciders.contentEquals(other.ciders)) return false
        if (ipv6 != other.ipv6) return false
        if (stunIp != other.stunIp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = port
        result = 31 * result + stunPort
        result = 31 * result + lastActive.hashCode()
        result = 31 * result + identity.hashCode()
        result = 31 * result + privateIp.hashCode()
        result = 31 * result + ciders.contentHashCode()
        result = 31 * result + ipv6.hashCode()
        result = 31 * result + stunIp.hashCode()
        return result
    }
}