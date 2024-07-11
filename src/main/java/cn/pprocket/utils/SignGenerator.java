package cn.pprocket.utils;

import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignGenerator {
    @SneakyThrows
    public static String md5(String str) {
        try {
            // 创建 MD5 的 MessageDigest 实例
            MessageDigest digest = MessageDigest.getInstance("MD5");

            // 更新 MessageDigest 对象
            digest.update(str.getBytes());

            // 计算哈希值
            byte[] hashBytes = digest.digest();

            // 将哈希值转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static class ScopeL {
        String[] alphas = new String[]{"a", "b", "e", "g", "h", "i", "m", "n", "o", "p", "q", "r", "s", "t", "u", "w"};
        int[] t = {0, 0, 0, 0};

        int a(int e) {
            return (128 & e) != 0 ? 255 & (e << 1 ^ 27) : e << 1;
        }

        int o(int e) {
            return a(e) ^ e;
        }

        int s(int e) {
            return o(a(e));
        }

        int r(int e) {
            return s(o(a(e)));
        }

        int c(int e) {
            return r(e) ^ s(e) ^ o(e);
        }

        int[] invoke(int[] e) {
            t[0] = c(e[0]) ^ r(e[1]) ^ s(e[2]) ^ o(e[3]);
            t[1] = o(e[0]) ^ c(e[1]) ^ r(e[2]) ^ s(e[3]);
            t[2] = s(e[0]) ^ o(e[1]) ^ c(e[2]) ^ r(e[3]);
            t[3] = r(e[0]) ^ s(e[1]) ^ o(e[2]) ^ c(e[3]);
            e[0] = t[0];
            e[1] = t[1];
            e[2] = t[2];
            e[3] = t[3];
            return e;
        }
    }

    private int r(int[] e) {
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            sum += e[i];
        }
        return sum;
    }

    private static String padStart(String originalString, int targetLength, char padChar) {
        if (originalString.length() >= targetLength) {
            return originalString;
        }

        StringBuilder sb = new StringBuilder();
        int numberOfPads = targetLength - originalString.length();

        for (int i = 0; i < numberOfPads; i++) {
            sb.append(padChar);
        }

        sb.append(originalString);

        return sb.toString();
    }

    public String hkey(String path, int time, String nonce) {
        time++;
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        String s = "JKMNPQRTX1234OABCDFG56789H";
        String s1 = md5((nonce + s).replaceAll("[a-zA-Z]", ""));
        String s2 = md5(time + path + s1);
        String m = s2.replaceAll("[^0-9]", "").substring(0, 9);
        while (m.length() != 9) {
            m = m + "0";
        }
        int n = Integer.parseInt(m);
        String a = "";
        for (int i = 0; i < 5; i++) {
            int t = n % s.length();
            n = (int)  (n / s.length());
            a = a + s.charAt(t);
        }
        String[] chars = a.substring(a.length() - 4).split("");
        int[] codes = new int[4];
        for (int i = 0; i < 4; i++) {
            codes[i] = chars[i].getBytes()[0];
        }
        String d = padStart(String.valueOf(r(new ScopeL().invoke(codes)) % 100), 2, '0');
        return a + d;
    }

    public static void main(String[] args) {
        SignGenerator signGenerator = new SignGenerator();
        String hkey = signGenerator.hkey("/bbs/web/profile/post/comments/", 1720601926, "DDA431CBF89F917391FA555D0ACAA42D");
        System.out.println(hkey);
    }
}

