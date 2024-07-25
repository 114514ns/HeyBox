package org.example

import cn.pprocket.HeyClient
import cn.pprocket.HeyClient.getComments
import cn.pprocket.items.Topic
import java.io.File


fun main() {

    HeyClient.login(File("cookie.txt").readText().replace("[\r\n]".toRegex(), ""))
    val url = HeyClient.genQRCode()
    println(url)
    while (true) {
        HeyClient.checkLogin(url)
        Thread.sleep(1000)
    }


}
