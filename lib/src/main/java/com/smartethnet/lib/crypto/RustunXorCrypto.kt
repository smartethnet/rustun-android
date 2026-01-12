package com.smartethnet.lib.crypto

import kotlin.experimental.xor

class RustunXorCrypto(val secret: String) : RustunCrypto {

    private val _keyArray = secret.toByteArray(Charsets.UTF_8)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun encrypt(data: ByteArray): ByteArray {
        for (index in 0..<data.size) {
            data[index] = data[index].xor(_keyArray[index % _keyArray.size])
        }
        return data
    }

    override fun decrypt(data: ByteArray): ByteArray {
        return encrypt(data)
    }

}