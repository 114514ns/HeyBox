package cn.pprocket.utils

import cn.pprocket.Platform
import io.ktor.utils.io.core.*
import korlibs.crypto.MD5
import kotlin.random.Random


class ParamsBuilder(private val maps: Map<String, String>) {
    @OptIn(ExperimentalStdlibApi::class)
    private fun nonce(): String {
        return MD5.digest(Random.nextDouble().toString().toByteArray()).hex
    }
    fun build(path:String): String {
        var time = (Platform.currentTimeMillis() / 1000).toString()
        val hash = nonce()
        val builtin = mapOf(
            "client_type" to "heybox_chat",
            "x_client_type" to "web",
            "os_type" to "web",
            "x_os_type" to "Windows",
            "device_info" to "Chrome",
            "x_app" to "heybox_chat",
            "version" to "999.0.3",
            "web_version" to "1.0.0",
            "chat_os_type" to "web",
            "chat_version" to "1.24.4",
            "chat_exe_version" to "",
            "heybox_id" to "36331242",
            "nonce" to hash,
            "_time" to time,
            "hkey" to SignGenerator().hkey(path,time.toInt(),hash),
            "_chat_time" to (time.toInt() * 10000 + 413),
            "imei" to "58dcf9f48bba35a0",
            "build" to "783"
            )
        var tmp = maps.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
        return "$tmp&" + builtin.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
    }
}
