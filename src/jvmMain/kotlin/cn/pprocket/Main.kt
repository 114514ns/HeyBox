package cn.pprocket

import cn.pprocket.HeyClient.fetchComments
import cn.pprocket.items.Comment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.function.Function
import java.util.stream.Collectors


suspend fun main(args: Array<String>) {

   println(HeyClient.getComments("145840745",1))


}
