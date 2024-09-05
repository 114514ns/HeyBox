package cn.pprocket

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.cookies.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.get
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

actual object Platform {
    actual val name: String = "JS"
    actual fun getKtorClient(): HttpClient {
        return HttpClient(Js) {
            install(HeaderInterceptor)
            install(HttpCookies)
        }
    }

    // 将 Promise 转换为 suspend 函数

    // suspend 函数读取剪贴板图片并转换为 ByteArray
    actual fun getClipImage(): ByteArray? {
        return null
    }

    actual fun currentTimeMillis(): Long {
        return kotlin.js.Date.now().toLong()
    }

}
