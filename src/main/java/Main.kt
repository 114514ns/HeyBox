package org.example

import cn.pprocket.HeyClient
import cn.pprocket.items.Topic
import java.io.File


fun main() {

    println("Hello World!")
    HeyClient.scriptContent = File("extra.js").readText()
    HeyClient.login("pkey=MTcwODY3NTg1MS41NF8zNjMzMTI0MmRjcnRwZXNiZGxwemhic3o__;x_xhh_tokenid=BAJCrLrAMOH4/SkA76ynCBKbajL/uOjmpDAXO982aiQrcn3oqS+MAAEFGrV4Jvfv+Bib1MYejU0cioWEIVVjXJw==")
    //var comments = HeyClient.getComments("127643195", 1)
    var posts = HeyClient.getPosts(Topic.SCHOOL)
    var post = posts[0]
    println(post.fillContent())


}
