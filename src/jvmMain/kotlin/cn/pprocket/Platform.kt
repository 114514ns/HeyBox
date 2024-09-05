package cn.pprocket

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.cookies.*
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual object Platform {
    actual val name: String = "JVM"
    actual fun getKtorClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(HeaderInterceptor)
            install(HttpCookies)
        }
    }
    actual fun getClipImage(): ByteArray? {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            val image = clipboard.getData(DataFlavor.imageFlavor) as? Image
            if (image != null) {
                val bufferedImage = toBufferedImage(image)
                val outputStream = ByteArrayOutputStream()
                ImageIO.write(bufferedImage, "png", outputStream)
                return outputStream.toByteArray()
            }
        }
        return null
    }
    actual fun currentTimeMillis() : Long {
        return System.currentTimeMillis()
    }

    private fun toBufferedImage(image: Image): BufferedImage {
        if (image is BufferedImage) return image
        val bufferedImage = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.createGraphics()
        graphics.drawImage(image, 0, 0, null)
        graphics.dispose()
        return bufferedImage
    }
}
