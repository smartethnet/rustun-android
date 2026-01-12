package com.smartethnet.rustun.util

import java.net.InetAddress

/**
 * 网络工具类
 */
object NetworkUtil {

    /**
     * 根据掩码长度生成掩码
     * @param prefixLength 掩码长度
     * @param addressLength IP地址长度（4 for IPv4, 16 for IPv6）
     * @return 掩码字节数组
     */
    fun createMask(prefixLength: Int, addressLength: Int): ByteArray {
        var prefix = prefixLength
        val mask = ByteArray(addressLength)
        for (i in 0..<mask.size) {
            if (prefix >= 8) {
                mask[i] = 0xFF.toByte() // 255
                prefix -= 8
            } else if (prefix > 0) {
                mask[i] = (0xFF shl (8 - prefix)).toByte() // 部分掩码
                prefix = 0
            } else {
                mask[i] = 0 // 剩余部分为0
            }
        }
        return mask
    }

    /**
     * 判断IP是否属于某个网段
     *
     * @param ipAddress 要检查的IP地址
     * @param subnet    网段，格式如 "192.168.1.201/32"
     * @return true 如果IP属于该网段，否则 false
     */
    fun isIPInSubnet(ipAddress: String, subnet: String): Boolean {
        // 分割网段和掩码
        val subnetParts: Array<String> =
            subnet.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        require(subnetParts.size == 2) { "Invalid subnet format. Expected format: x.x.x.x/y" }

        // 网段地址
        val subnetAddress = subnetParts[0]
        // 掩码长度
        val prefixLength = subnetParts[1].toInt()

        // 将IP地址和网段地址转换为字节数组
        try {
            val ipBytes = InetAddress.getByName(ipAddress).address
            val subnetBytes = InetAddress.getByName(subnetAddress).address

            // 检查IP地址和网段地址的长度是否一致
            require(ipBytes.size == subnetBytes.size) { "IP address and subnet address are not the same type (IPv4/IPv6)." }

            // 计算掩码
            val mask = createMask(prefixLength, ipBytes.size)

            // 检查IP地址是否属于该网段
            for (i in ipBytes.indices) {
                if ((ipBytes[i].toInt() and mask[i].toInt()) != (subnetBytes[i].toInt() and mask[i].toInt())) {
                    return false
                }
            }

            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun ipToInt(ipAddress: String): Long {
        val parts = ipAddress.split(".")
        require(parts.size == 4) { "无效的IP地址格式" }

        return (parts[0].toLong() shl 24) or
                (parts[1].toLong() shl 16) or
                (parts[2].toLong() shl 8) or
                parts[3].toLong()
    }

    fun intToIp(ip: Long): String {
        return "${(ip shr 24) and 0xFF}." +
                "${(ip shr 16) and 0xFF}." +
                "${(ip shr 8) and 0xFF}." +
                "${ip and 0xFF}"
    }

    /**
     * 根据IP和子网掩玛计算网段
     */
    fun calculateNetworkSegment(ipAddress: String, subnetMask: String): String {
        // 将IP地址和子网掩码转换为整数
        val ipInt = ipToInt(ipAddress)
        val maskInt = ipToInt(subnetMask)

        // 计算网段（按位与操作）
        val networkInt = ipInt and maskInt

        // 将结果转换回IP地址格式
        return intToIp(networkInt)
    }
}