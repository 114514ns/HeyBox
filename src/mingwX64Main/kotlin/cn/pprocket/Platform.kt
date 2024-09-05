package cn.pprocket

import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.engine.curl.*
import kotlinx.datetime.Clock

actual object Platform {

    actual fun getKtorClient(): HttpClient {
        return HttpClient(Curl) {
            install(HeaderInterceptor)
            install(HttpCookies)
        }
    }
    actual fun getClipImage(): ByteArray? {
        return null
    }

    actual fun currentTimeMillis(): Long {
        return Clock.System.now().epochSeconds
    }

    actual val name: String
        get() = "Native"
}
