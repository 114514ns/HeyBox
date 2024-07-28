package org.example

import cn.pprocket.HeyClient
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.io.File


fun main() {

    HeyClient.login(File("cookie.txt").readText().replace("[\r\n]".toRegex(), ""))
    HeyClient.getCloudToken(getImageFromClipboard()!!)

}
@Throws(Exception::class)
fun getImageFromClipboard(): Image? {
    val sysc: Clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
    val cc = sysc.getContents(null)
    if (cc == null) return null
    else if (cc.isDataFlavorSupported(DataFlavor.imageFlavor)) return cc.getTransferData(DataFlavor.imageFlavor) as Image
    return null
}
