package cn.pprocket

import cn.pprocket.HeyClient.fetchComments
import cn.pprocket.items.Comment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.function.Function
import java.util.stream.Collectors


suspend fun main(args: Array<String>) {

   val user = HeyClient.getUser("56585052")
   val list = mutableListOf<Comment>()
   val map = mutableMapOf<String,Int>()
   for (i in 1..101) {
      val fetch = user.fetchComments(i)
      fetch.forEach {
         if (map.containsKey(it as String)) {
            map[it] = map[it]!! + 1
         } else {
            map[it] = 1
         }
      }
      list.addAll(fetch)

   }
   val str = Json.encodeToString(list)
   val f = File("lists.json")

   if (!f.exists()) {
      f.createNewFile()
   }

   f.writeText(str)



}
