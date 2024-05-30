package cn.pprocket.utils

import cn.pprocket.SignService
import java.security.MessageDigest
import java.util.*

class ParamsBuilder(private val maps: Map<String, String>) {
    private fun nonce(): String {
        val base = "Gt4NVYMeiA3cA45jCvj3on4Brh5V8nDe"
        val random = Random()
        val randomIndices = (0 until base.length).toList().shuffled().take(2)
        val randomChars = ('a'..'z').toList() + ('A'..'Z').toList() + ('0'..'9').toList()

        return base.mapIndexed { index, char ->
            if (index in randomIndices) {
                randomChars[random.nextInt(randomChars.size)]
            } else {
                char
            }
        }.joinToString("")
    }
    fun build(path:String): String {
        var time = (System.currentTimeMillis() / 1000).toString()
        val hash = nonce()
        val builtin = mapOf(
            "x_app" to "heybox",
            "build" to "821",
            "version" to "1.3.312",
            "os_version" to "12",
            "x_client_type" to "mobile",
            "x_os_type" to "Android",
            "os_type" to "android",
            "device_info" to "M2104K10AC",
            "imei" to "58dcf9f48bba35a5",
            "nonce" to hash,
            "_time" to time,
            "hkey" to SignService.calc(path,time,hash)
            )
        var tmp = maps.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
        return "$tmp&" + builtin.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
    }
}
