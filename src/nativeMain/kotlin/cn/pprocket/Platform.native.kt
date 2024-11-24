package cn.pprocket

import io.ktor.client.*

actual object Platform {
    actual val name: String
        get() = TODO("Not yet implemented")

    actual fun getKtorClient(): HttpClient {
        TODO("Not yet implemented")
    }

    actual fun getClipImage(): ByteArray? {
        TODO("Not yet implemented")
    }

    actual fun currentTimeMillis(): Long {
        TODO("Not yet implemented")
    }
}
