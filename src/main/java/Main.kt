package org.example

import cn.pprocket.HeyClient
import cn.pprocket.HeyClient.getComments
import cn.pprocket.items.Topic
import java.io.File


fun main() {

    HeyClient.login(File("cookie.txt").readText().replace("[\r\n]".toRegex(), ""))
    //var comments = HeyClient.getComments("127643195", 1)
    //HeyClient.getComments("126700671",1)
    println(HeyClient.genQRCode())


}
