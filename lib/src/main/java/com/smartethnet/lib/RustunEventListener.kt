package com.smartethnet.lib

import com.smartethnet.lib.message.DataMessage
import com.smartethnet.lib.message.HandShakeReplyMessage
import com.smartethnet.lib.message.KeepAliveMessage
import com.smartethnet.lib.message.ProbeHolePunchMessage
import com.smartethnet.lib.message.ProbeIpv6Message

interface RustunEventListener {

    fun onConnected()

    fun onDisconnected()

    fun onError(throws: Throwable?) {}

    fun onHandShakeReplayMessage(message: HandShakeReplyMessage) {}

    fun onProbeIpv6Message(message: ProbeIpv6Message) {}

    fun onProbeHolePunchMessage(message: ProbeHolePunchMessage) {}

    fun onDataMessage(message: DataMessage) {}

    fun onKeepAliveMessage(message: KeepAliveMessage) {}
}