package cn.pprocket

import io.ktor.client.*

expect object Platform {
    val name: String
    fun getKtorClient(): HttpClient
    fun getClipImage(): ByteArray?
    fun currentTimeMillis(): Long
}
