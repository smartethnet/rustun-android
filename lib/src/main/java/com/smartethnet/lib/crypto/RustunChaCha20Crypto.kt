package com.smartethnet.lib.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class RustunChaCha20Crypto(val secret: String) : RustunCrypto {

    private var key: SecretKeySpec
    private val random = SecureRandom()

    companion object {
        const val KEY_SIZE = 32
        const val NONCE_LENGTH = 12
        const val CHA_CHA_20_POLY_ALG = "ChaCha20-Poly1305"
    }

    init {
        val bytes = secret.toByteArray(Charsets.UTF_8)
        val keyBytes = ByteArray(KEY_SIZE)
        if (bytes.size >= KEY_SIZE) {
            System.arraycopy(bytes, 0, keyBytes, 0, KEY_SIZE)
        } else {
            System.arraycopy(bytes, 0, keyBytes, 0, bytes.size)
        }

        this.key = SecretKeySpec(keyBytes, CHA_CHA_20_POLY_ALG)
    }

    override fun encrypt(data: ByteArray): ByteArray {
        // 生成 12 字节的随机 nonce（IV）
        val nonce = generateNonce()

        // 初始化加密器
        val cipher = Cipher.getInstance(CHA_CHA_20_POLY_ALG, BouncyCastleProvider())
        val parameterSpec = IvParameterSpec(nonce)
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)

        // 执行加密
        val cipherText = cipher.doFinal(data)

        // 组合 nonce 和密文：nonce + cipherText
        val result = ByteArray(nonce.size + cipherText.size)
        System.arraycopy(nonce, 0, result, 0, nonce.size)
        System.arraycopy(cipherText, 0, result, nonce.size, cipherText.size)

        return result
    }

    override fun decrypt(data: ByteArray): ByteArray {
        // 分离 nonce 和密文（前 12 字节是 nonce）
        require(data.size >= RustunAes256Crypto.Companion.NONCE_LENGTH) { "data too short" }

        val nonce = data.copyOfRange(0, RustunAes256Crypto.Companion.NONCE_LENGTH)
        val cipherText = data.copyOfRange(RustunAes256Crypto.Companion.NONCE_LENGTH, data.size)

        // 初始化加密器
        val cipher = Cipher.getInstance(CHA_CHA_20_POLY_ALG, BouncyCastleProvider())
        val parameterSpec = IvParameterSpec(nonce)
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)

        // 执行解密
        return cipher.doFinal(cipherText)
    }

    private fun generateNonce(): ByteArray {
        // 生成 12 字节的随机 nonce
        val nonce = ByteArray(NONCE_LENGTH)
        random.nextBytes(nonce)
        return nonce
    }
}