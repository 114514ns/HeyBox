package cn.pprocket

import cn.pprocket.items.Topic

suspend fun main(args: Array<String>) {
    var t = HeyClient.searchSuggestion("只因")
    println()
}
