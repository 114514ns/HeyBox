package org.example.cn.pprocket.utils.app


import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


object AppSignGenerator {

    @OptIn(ExperimentalEncodingApi::class)
    fun hkey(path: String, time: String, nonce: String): String {
        val base64 = Base64.encode(path.toByteArray())
        val result = CharArray(5)
        //HeyBox heyBox = new HeyBox();
        //heyBox.init();
        var timeNum = time.toLong()
        var nums = 0
        for (b in nonce.toByteArray()) {
            if (b >= 48 && b <= 57) {
                nums++
            }
        }
        timeNum = timeNum + nums
        val v24 = Char((timeNum shr 24).toUShort())
        val v16 = Char((timeNum shr 16).toUShort())
        val v8 = Char((timeNum shr 8).toUShort())
        val bytes = ByteArray(8)
        val v77 = ("23456789BCDFGHJKMNPQRTVWXY" + nonce.uppercase())

        bytes[0] = 0.toByte()
        bytes[1] = 0
        bytes[2] = 0
        bytes[3] = 0
        bytes[4] = v24.code.toByte()
        bytes[5] = v16.code.toByte()
        bytes[6] = v8.code.toByte()
        bytes[7] = timeNum.toByte()
        val dist: ByteArray = genHMAC(bytes, base64.toByteArray())
        val reverse = ByteArray(4)
        val offset = dist[19].toInt() and 0xf
        reverse[0] = dist[offset]
        reverse[1] = dist[offset + 1]
        reverse[2] = dist[offset + 2]
        reverse[3] = dist[offset + 3]
        val v34: Int = byteArrayToLong(reverse)
        var v35: Int = bswap32(v34)
        v35 = v34
        val v36 = v35 and 0x7FFFFFFF
        var v37 = 1307386003L * ((v35 shr 2) and 0x1FFFFFFF)
        v37 = v77[((v37 shr 40) % 0x3A).toInt()].toLong()
        val v38 = (v35 and 0x7FFFFFFF) / 0x3A
        val v39 = v77[v36 - 58 * v38].toInt()
        val v40 = v77[v38 % 0x3A].toInt()
        val v41 = v77[v36 / 0x2FA28 % 0x3A].toInt()
        val v42 = v77[v36 / 0xACAD10 % 0x3A].toInt()
        result[0] = v39.toChar()
        result[1] = v40.toChar()
        result[2] = Char(v37.toUShort())
        result[3] = v41.toChar()
        result[4] = v42.toChar()

        val array = IntArray(4)
        array[0] = result[1].code
        array[1] = result[2].code
        array[2] = result[3].code
        array[3] = result[4].code
        sub_249C(array)
        val v43 = (array[0] + array[1] + array[2] + array[3]) % 100
        val num = formatWithLeadingZeros(v43)
        return result.concatToString() + num
    }
    fun formatWithLeadingZeros(value: Int, minLength: Int = 2): String {
        val stringValue = value.toString()
        return if (stringValue.length >= minLength) {
            stringValue
        } else {
            "0".repeat(minLength - stringValue.length) + stringValue
        }
    }

    fun bswap32(x: Int): Int {
        return ((x shl 24) and -0x1000000) or
                ((x shl 8) and 0x00ff0000) or
                ((x shr 8) and 0x0000ff00) or
                ((x shr 24) and 0x000000ff)
    }


    fun byteArrayToLong(bytes: ByteArray): Int {
        return (bytes[0].toInt() and 0xFF shl 24) or
                (bytes[1].toInt() and 0xFF shl 16) or
                (bytes[2].toInt() and 0xFF shl 8) or
                (bytes[3].toInt() and 0xFF)
    }


    fun genHMAC(data: ByteArray, key: ByteArray): ByteArray {
        val blockSize = 64  // SHA-1 block size in bytes

        // Step 1: Pad the key to the block size
        val actualKey = if (key.size > blockSize) sha1(key) else key
        val paddedKey = actualKey.copyOf(blockSize)

        // Step 2: Create inner and outer padded keys
        val innerKeyPad = ByteArray(blockSize) { i -> (paddedKey[i] xor 0x36).toByte() }
        val outerKeyPad = ByteArray(blockSize) { i -> (paddedKey[i] xor 0x5C).toByte() }

        // Step 3: Compute the inner hash
        val innerHashInput = innerKeyPad + data
        val innerHash = sha1(innerHashInput)

        // Step 4: Compute the outer hash
        val outerHashInput = outerKeyPad + innerHash
        return sha1(outerHashInput)
    }

    fun sub_249C(result: IntArray) {
        var v5: Int // w14
        var v6: Int // w1
        val v7: Int // w15
        var v9: Int // w16
        var v11: Int // w4
        var v12: Int // w6
        var v13: Int // w5
        var v14: Int // w20
        val v15: Int // w4
        var v16: Int // w17
        val v17: Int // w19
        val v18: Int // w9
        var v20: Int // w2
        val v22: Int // w4
        val v25: Int // w17
        var v26: Boolean // zf
        val v27: Int // w13
        var v28: Int // w15
        val v29: Int // w4
        val v30: Int // w10
        var v31: Int // w11
        val v34: Int // w13
        val v35: Int // w10
        val v39: Int // w9
        var v40: Int // w13
        var v42: Int // w8

        val v1 = result[0] // w9
        val v2 = result[1] // w10
        val v3 = result[2] // w11
        val v4 = result[3] // w13
        v5 = 2 * v4
        v6 = 2 * v2
        v7 = (2 * v4) and 0xFE xor 0x1B
        val v8 = if ((result[0] and 0x80) != 0) 2 * result[0] and 0xFE xor 0x1B
        else 2 * result[0] // w3
        v9 = (2 * v8) and 0xFE xor 0x1B
        if ((v8 and 0x80) == 0) v9 = 2 * v8
        val v10 = v9 xor v8 // w12
        v11 = 2 * (v9 xor v8)
        if (((v9 xor v8) and 0x80) != 0) v11 = ((2 * (v9 xor v8)) and 0xff) xor 0x1B
        v12 = 2 * v11
        if ((v11 and 0x80) != 0) v12 = (2 * v11) and 0xFE xor 0x1B
        if ((v2 and 0x80) != 0) v6 = ((2 * v2) and 0xff) xor 0x1B
        v13 = 2 * v6
        if ((v6 and 0x80) != 0) v13 = (2 * v6) and 0xFE xor 0x1B
        v14 = 2 * (v13 xor v6)
        if (((v13 xor v6) and 0x80) != 0) v14 = ((2 * (v13 xor v6)) and 0xff) xor 0x1B
        v15 = v11 xor v4
        v16 = 2 * v3
        v17 = v15 xor v1
        v18 = v14 xor v1 xor v2 xor v8
        val v19 = if ((v14 and 0x80) != 0) 2 * v14 and 0xFE xor 0x1B
        else 2 * v14 // w3
        if ((v3 and 0x80) != 0) v16 = (2 * v3) and 0xFE xor 0x1B
        v20 = 2 * v16
        val v21 = v18 xor v13 // w9
        if ((v16 and 0x80) != 0) v20 = (2 * v16) and 0xFE xor 0x1B
        v22 = v15 xor v3
        val v23 = v20 xor v16 // w13
        if ((result[3] and 0x80) != 0) v5 = v7
        val v24 = v22 xor v16 // w4
        v25 = 2 * v23
        v26 = (v23 and 0x80) == 0
        v27 = v17 xor v5 xor v9 xor v23
        v28 = v25 and 0xFE xor 0x1B
        if (v26) v28 = v25
        v29 = v24 xor v13 xor v6
        v30 = v28 xor v2 xor v3
        v31 = (2 * v28) and 0xFE xor 0x1B
        val v32 = v27 xor v14 xor v12 // w13
        val v33 = v30 xor v6 // w10
        if ((v28 and 0x80) == 0) v31 = 2 * v28
        v34 = v32 xor v19
        v35 = v33 xor v10
        val v36 = if ((v5 and 0x80) != 0) 2 * v5 and 0xff xor 0x1B
        else 2 * v5 // w12
        result[0] = v34
        val v37 = v36 xor v5 // w13
        val v38 = 2 * (v36 xor v5) // w14
        v39 = v21 xor v37
        v26 = (v37 and 0x80) == 0
        v40 = v38 and 0xFE xor 0x1B
        val v41 = v39 xor v28 xor v19 // w9
        if (v26) v40 = v38
        result[1] = v41 xor v31
        v42 = (2 * v40) and 0xFE xor 0x1B
        val v43 = v35 xor v20 xor v40 xor v31 // w10
        if ((v40 and 0x80) == 0) v42 = 2 * v40
        result[2] = v43 xor v42
        result[3] = v29 xor v36 xor v40 xor v12 xor v42
    }
}
