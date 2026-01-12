package com.smartethnet.lib.message

import com.google.gson.annotations.SerializedName

data class KeepAliveMessage(
    @SerializedName("identity")
    val identity: String,

    @SerializedName("ipv6")
    val ipv6: String,

    @SerializedName("port")
    val port: Int,

    @SerializedName("stun_ip")
    val stunIp: String,

    @SerializedName("stun_port")
    val stunPort: Int,

    @SerializedName("peer_details")
    val peerDetails: Array<PeerDetail>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeepAliveMessage

        if (port != other.port) return false
        if (stunPort != other.stunPort) return false
        if (identity != other.identity) return false
        if (ipv6 != other.ipv6) return false
        if (stunIp != other.stunIp) return false
        if (!peerDetails.contentEquals(other.peerDetails)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = port
        result = 31 * result + stunPort
        result = 31 * result + identity.hashCode()
        result = 31 * result + ipv6.hashCode()
        result = 31 * result + stunIp.hashCode()
        result = 31 * result + peerDetails.contentHashCode()
        return result
    }
}