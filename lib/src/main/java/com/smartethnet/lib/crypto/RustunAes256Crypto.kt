package com.smartethnet.lib.crypto

class RustunAes256Crypto : RustunCrypto {
    override fun encrypt(data: ByteArray): ByteArray {
        return data
    }

    override fun decrypt(data: ByteArray): ByteArray {
        return data
    }
}