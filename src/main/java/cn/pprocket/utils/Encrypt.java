package cn.pprocket.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Encrypt {
    public static String HMAC_SHA1(String s1, String s2) {
        try {
            // 创建一个密钥规格，使用 HmacSHA1 算法
            SecretKeySpec signingKey = new SecretKeySpec(s2.getBytes(), "HmacSHA1");

            // 创建 Mac 实例并初始化
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // 计算 HMAC 值
            byte[] rawHmac = mac.doFinal(s1.getBytes());

            // 将 HMAC 值编码为 Base64 字符串
            return bytesToHex(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA1", e);
        }
    }

    public static String SHA1(String rawHmac) {
        try {
            // 创建一个 SHA-1 实例
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");

            // 计算 SHA-1 值
            byte[] sha1Hash = md.digest(rawHmac.getBytes("UTF-8"));

            // 将 SHA-1 值编码为 Base64 字符串
            return bytesToHex(sha1Hash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate SHA-1", e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b); // 将字节转换为无符号整数并转为十六进制字符串
            if (hex.length() == 1) {
                hexString.append('0'); // 如果是单字符，在前面补0
            }
            hexString.append(hex);
        }
        return hexString.toString().toLowerCase();
    }
}
