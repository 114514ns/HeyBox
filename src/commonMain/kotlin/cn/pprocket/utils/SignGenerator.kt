package cn.pprocket.utils


import io.ktor.utils.io.core.*
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

class SignGenerator {
    internal class ScopeL {
        var alphas: Array<String> =
            arrayOf("a", "b", "e", "g", "h", "i", "m", "n", "o", "p", "q", "r", "s", "t", "u", "w")
        @JvmField
        var t: IntArray = intArrayOf(0, 0, 0, 0)

        fun a(e: Int): Int {
            return if ((128 and e) != 0) 255 and (e shl 1 xor 27) else e shl 1
        }

        fun o(e: Int): Int {
            return a(e) xor e
        }

        fun s(e: Int): Int {
            return o(a(e))
        }

        fun r(e: Int): Int {
            return s(o(a(e)))
        }

        fun c(e: Int): Int {
            return r(e) xor s(e) xor o(e)
        }

        fun invoke(e: IntArray): IntArray {
            t[0] = c(e[0]) xor r(e[1]) xor s(e[2]) xor o(e[3])
            t[1] = o(e[0]) xor c(e[1]) xor r(e[2]) xor s(e[3])
            t[2] = s(e[0]) xor o(e[1]) xor c(e[2]) xor r(e[3])
            t[3] = r(e[0]) xor s(e[1]) xor o(e[2]) xor c(e[3])
            e[0] = t[0]
            e[1] = t[1]
            e[2] = t[2]
            e[3] = t[3]
            return e
        }
    }

    private fun r(e: IntArray): Int {
        var sum = 0
        for (i in 0..3) {
            sum += e[i]
        }
        return sum
    }

    fun hkey(path: String, time: Int, nonce: String): String {
        var path = path
        var time = time
        time++
        if (!path.endsWith("/")) {
            path = "$path/"
        }
        val s = "JKMNPQRTX1234OABCDFG56789H"
        val s1 = Encrypt.MD5((nonce + s).replace("[a-zA-Z]".toRegex(), ""))
        val s2 = Encrypt.MD5(time.toString() + path + s1)
        var m = s2.replace("[^0-9]".toRegex(), "").substring(0, 9)
        while (m.length != 9) {
            m = m + "0"
        }
        var n = m.toInt()
        var a = ""
        for (i in 0..4) {
            val t = n % s.length
            n = (n / s.length)
            a = a + s[t]
        }
        val chars = a.substring(a.length - 4).split("".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val codes = IntArray(4)
        for (i in 1..4) {
            codes[i-1] = chars[i].toByteArray().get(0).toInt()
        }
        val d = padStart((r(ScopeL().invoke(codes)) % 100).toString(), 2, '0')
        return a + d
    }

    companion object {


        private fun padStart(originalString: String, targetLength: Int, padChar: Char): String {
            if (originalString.length >= targetLength) {
                return originalString
            }

            val sb = StringBuilder()
            val numberOfPads = targetLength - originalString.length

            for (i in 0 until numberOfPads) {
                sb.append(padChar)
            }

            sb.append(originalString)

            return sb.toString()
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val signGenerator = SignGenerator()
            val hkey =
                signGenerator.hkey("/bbs/web/profile/post/comments/", 1720601926, "DDA431CBF89F917391FA555D0ACAA42D")
            println(hkey)
        }
    }
}


