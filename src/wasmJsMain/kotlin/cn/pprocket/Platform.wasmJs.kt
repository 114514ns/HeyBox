package cn.pprocket

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.cookies.*


actual object Platform {
    actual val name: String
        get() = TODO("Not yet implemented")

    actual fun getKtorClient(): HttpClient {
        return HttpClient(Js) {
            install(HeaderInterceptor)
            install(HttpCookies)
        }
    }

    actual fun getClipImage(): ByteArray? {
        TODO("Not yet implemented")
    }

    actual fun currentTimeMillis(): Long {
        TODO("Not yet implemented")
    }
}
