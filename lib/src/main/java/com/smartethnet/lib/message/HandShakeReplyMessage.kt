package com.smartethnet.lib.message

import com.google.gson.annotations.SerializedName

data class HandShakeReplyMessage(
    @SerializedName("private_ip")
    val privateIp: String,

    @SerializedName("mask")
    val mask: String,

    @SerializedName("gateway")
    val gateway: String,

    @SerializedName("peer_details")
    val peerDetails: Array<PeerDetail>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HandShakeReplyMessage

        if (privateIp != other.privateIp) return false
        if (mask != other.mask) return false
        if (gateway != other.gateway) return false
        if (!peerDetails.contentEquals(other.peerDetails)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = privateIp.hashCode()
        result = 31 * result + mask.hashCode()
        result = 31 * result + gateway.hashCode()
        result = 31 * result + peerDetails.contentHashCode()
        return result
    }
}