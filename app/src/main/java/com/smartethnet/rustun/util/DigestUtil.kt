package com.smartethnet.rustun.util

import java.security.MessageDigest

/**
 * 摘要算法工具类
 */
object DigestUtil {

    /**
     * 计算消息摘要SHA-1
     */
    fun sha1(message: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val hashBytes = digest.digest(message.toByteArray(Charsets.UTF_8))
        return hashBytes.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * 计算消息摘要SHA-256
     */
    fun sha256(message: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(message.toByteArray(Charsets.UTF_8))
        return hashBytes.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * 计算消息摘要SHA-512
     */
    fun sha512(message: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(message.toByteArray(Charsets.UTF_8))
        return hashBytes.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * 计算消息摘要MD5
     */
    fun md5(message: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(message.toByteArray(Charsets.UTF_8))
        return hashBytes.fold("") { str, it -> str + "%02x".format(it) }
    }
}