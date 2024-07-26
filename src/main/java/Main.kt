package org.example

import cn.pprocket.HeyClient
import cn.pprocket.HeyClient.fetchComments
import cn.pprocket.HeyClient.getComments
import cn.pprocket.items.Topic
import java.io.File


fun main() {

    HeyClient.login(File("cookie.txt").readText().replace("[\r\n]".toRegex(), ""))
    HeyClient.getUser("18888961").fetchComments(1)


}
