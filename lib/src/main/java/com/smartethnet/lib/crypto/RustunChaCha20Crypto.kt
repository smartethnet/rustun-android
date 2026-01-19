package com.smartethnet.lib.crypto

class RustunChaCha20Crypto(val secret: String) : RustunCrypto {
    override fun encrypt(data: ByteArray): ByteArray {
        return data
    }

    override fun decrypt(data: ByteArray): ByteArray {
        return data
    }
}