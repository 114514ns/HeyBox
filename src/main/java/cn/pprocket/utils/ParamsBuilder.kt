package cn.pprocket.utils

import cn.pprocket.HeyClient
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Value
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import javax.script.Invocable
import kotlin.text.HexFormat


class ParamsBuilder(private val maps: Map<String, String>) {
    @OptIn(ExperimentalStdlibApi::class)
    private fun nonce(): String {
        val random = Random().nextDouble()
        var instance = MessageDigest.getInstance("MD5")
        instance.update(random.toString().toByteArray())
        return instance.digest().toHexString(HexFormat.UpperCase)
    }
    fun build(path:String): String {
        var time = (System.currentTimeMillis() / 1000).toString()
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
            "heybox_id" to "-1",
            "nonce" to hash,
            "_time" to time,
            "hkey" to SignService.calc(path,time,hash),
            "_chat_time" to (time.toInt() * 10000 + 413)
            )
        var tmp = maps.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
        return "$tmp&" + builtin.map { (key, value) -> "${key}=${value}" }
            .joinToString("&")
    }
}
class SignService {
    companion object {
        var func : Value? = null
        fun calc(path: String, time: String, nonce: String): String {
            val args: MutableList<Any> = ArrayList(10)
            args.add(path)
            args.add(time)
            args.add(nonce)
            return func!!.execute(path,time, nonce).asString()
        }
        @OptIn(ExperimentalStdlibApi::class)
        fun md5(data: String): String {
            val md = MessageDigest.getInstance("MD5")
            md.update(data.toByteArray())
            return md.digest().toHexString()
        }
        init {
            val context = Context.newBuilder("js")
                .allowHostClassLookup { s: String? -> true }
                .allowHostAccess(HostAccess.ALL)
                .build()
            val script = HeyClient.scriptContent
            context.eval("js", script);
            func = context.getBindings("js").getMember("hkey")
        }
    }
}
