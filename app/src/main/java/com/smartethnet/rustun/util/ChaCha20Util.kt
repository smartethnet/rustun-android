package com.smartethnet.rustun.util

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Key
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * ChaCha20加解密工具类
 */
object ChaCha20Util {
    private const val ENCRYPT_ALG: String = "ChaCha20-Poly1305"
    private val random: SecureRandom = SecureRandom()

    fun createNonce(): ByteArray {
        val nonce = ByteArray(12)
        random.nextBytes(nonce)
        return nonce
    }

    /**
     * 加密
     */
    fun chaCha20Ploy1305Encrypt(message: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        return chaCha20Ploy1305(message, key, nonce, Cipher.ENCRYPT_MODE)
    }

    /**
     * 解密
     */
    fun chaCha20Ploy1305Decrypt(message: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        return chaCha20Ploy1305(message, key, nonce, Cipher.DECRYPT_MODE)
    }

    /**
     * ChaCha20加解密算法
     */
    private fun chaCha20Ploy1305(
        message: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        mode: Int
    ): ByteArray {
        // nonce 长度必须为 12字节
        require(nonce.size == 12) { "nonce must be 12 bytes in length" }
        // 密钥的长度必须为256位，即32字节
        require(key.size == 32) { "key length must be 256 bits" }

        // 加载密钥
        val theKey: Key = SecretKeySpec(key, ENCRYPT_ALG)
        // 配置参数
        val spec: AlgorithmParameterSpec = IvParameterSpec(nonce)
        // 执行加解密
        try {
            val cipher = Cipher.getInstance(ENCRYPT_ALG, BouncyCastleProvider())
            cipher.init(mode, theKey, spec)
            return cipher.doFinal(message)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}