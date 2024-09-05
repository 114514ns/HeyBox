package cn.pprocket.utils

import io.ktor.utils.io.core.*
import korlibs.crypto.HMAC
import korlibs.crypto.MD5

object Encrypt {
    fun HMAC_SHA1(s1: String, s2: String): String {
        return HMAC.hmacSHA1(s1.toByteArray(), s1.toByteArray()).hex
    }

    fun SHA1(rawHmac: String): String {
        return korlibs.crypto.SHA1.digest(rawHmac.toByteArray()).hex
    }
    fun MD5(message:String) : String {
        return MD5.digest(message.toByteArray()).hex
    }


}

