package cn.pprocket.utils.app

import cn.pprocket.Platform
import org.example.cn.pprocket.utils.app.AppSignGenerator
import kotlin.random.Random

class AppParamsBuilder(private val maps: Map<String, String>) {
    @OptIn(ExperimentalStdlibApi::class)
    fun nonce(): String {
        val random = Random
        val stringBuilder = StringBuilder()
        for (i2 in 0..31) {
            stringBuilder.append("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"[random.nextInt(62)])
        }
        return stringBuilder.toString()
    }

    fun build(path:String): String {
        var time = (Platform.currentTimeMillis() / 1000).toString()
        //val time = "1721618176"
        val hash = nonce()
        //val hash = "mcfuUBmVtL9fXAFIXoQsOLBYNOFFuzCt"
        val builtin = mapOf(
            "imei" to "58dcf9f48bba35a",
            "device_info" to "M2104K10AC",
            "os_type" to "Android",
            "x_os_type" to "Android",
            "x_client_type" to "mobile",
            "os_version" to "14",
            "heybox_id" to "36331242",
            "version" to "1.3.232",
            "_time" to time,
            "nonce" to hash,
            "hkey" to AppSignGenerator.hkey(
                path,
                time,
                hash
            ),
            "channel" to "heybox_wandoujia",
            "x_app" to "heybox",
            "netmode" to "wifi",
            "dw" to "393"

        )
        var tmp = maps.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
        return "$tmp&" + builtin.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
    }
}
