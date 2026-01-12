package com.smartethnet.rustun.util

import org.junit.Assert
import org.junit.Test

class NetworkUtilTest {

    @Test
    fun createMask() {
        val mask = NetworkUtil.createMask(24, 4)
        Assert.assertEquals(4, mask.size)
        Assert.assertEquals(0xFF.toByte(), mask[0])
        Assert.assertEquals(0xFF.toByte(), mask[1])
        Assert.assertEquals(0xFF.toByte(), mask[2])
        Assert.assertEquals(0x00.toByte(), mask[3])
    }

    @Test
    fun isIPInSubnet() {
        Assert.assertFalse(NetworkUtil.isIPInSubnet("192.168.1.100", "192.168.1.201/32"))
        Assert.assertTrue(NetworkUtil.isIPInSubnet("192.168.1.201", "192.168.1.201/32"))
        Assert.assertTrue(NetworkUtil.isIPInSubnet("192.168.1.100", "192.168.1.0/24"))
    }
}