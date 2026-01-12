package com.smartethnet.lib.crypto

interface RustunCrypto {

    fun encrypt(data: ByteArray): ByteArray
    fun decrypt(data: ByteArray): ByteArray

}