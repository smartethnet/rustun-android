package com.smartethnet.rustun.util

import org.junit.Assert
import org.junit.Test

class DigestUtilTest {

    @Test
    fun sha1() {
        val message = "hello"
        Assert.assertEquals(
            "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d",
            DigestUtil.sha1(message)
        )
    }

    @Test
    fun sha256() {
        val message = "hello"
        Assert.assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            DigestUtil.sha256(message)
        )
    }

    @Test
    fun sha512() {
        val message = "hello"
        Assert.assertEquals(
            "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043",
            DigestUtil.sha512(message)
        )
    }

    @Test
    fun md5() {
        val message = "hello"
        Assert.assertEquals(
            "5d41402abc4b2a76b9719d911017c592", DigestUtil.md5(message)
        )
    }
}