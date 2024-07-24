package org.example

import cn.pprocket.HeyClient
import cn.pprocket.HeyClient.getComments
import cn.pprocket.items.Topic
import java.io.File


fun main() {

    HeyClient.login(File("cookie.txt").readText().replace("[\r\n]".toRegex(), ""))
    HeyClient.getPost("126188881")


}
