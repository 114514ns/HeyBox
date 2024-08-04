package org.example

import cn.pprocket.HeyClient
import cn.pprocket.HeyClient.getFollowers
import cn.pprocket.items.Topic
import org.example.cn.pprocket.utils.app.AppSignGenerator
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.io.File


fun main() {

    HeyClient.login(File("cookie.txt").readText().replace("[\r\n]".toRegex(), ""))
    var time = System.currentTimeMillis()/1000
    time = 1722566880
    //AppSignGenerator.hkey("/bbs/app/feeds/", time.toString(),"262088DD2A869D06DE2FF8ACE593AC24")
    HeyClient.getPosts(Topic.RECOMMEND)

}
@Throws(Exception::class)
fun getImageFromClipboard(): Image? {
    val sysc: Clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
    val cc = sysc.getContents(null)
    if (cc == null) return null
    else if (cc.isDataFlavorSupported(DataFlavor.imageFlavor)) return cc.getTransferData(DataFlavor.imageFlavor) as Image
    return null
}
