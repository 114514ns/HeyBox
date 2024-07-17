package cn.pprocket.utils.app;

import org.example.cn.pprocket.utils.app.AppSignGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SignPool {
    public static SignPool instance = new SignPool();
    public List<Sign> signs = Collections.synchronizedList(new ArrayList<>());

    public SignPool() {
        new Thread(() -> {
            while (true) {
                System.out.println("当前剩余sign  " + signs.size());
                Sign sign = new Sign();
                sign.time = String.valueOf(System.currentTimeMillis() / 1000);
                sign.nonce = nonce();
                String hkey = AppSignGenerator.INSTANCE.hkey("/bbs/app/feeds/", sign.time, nonce());
                sign.hkey = hkey;
                instance.signs.add(sign);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    long now = System.currentTimeMillis() / 1000;
                    for (int i = signs.size() - 1; i >= 0; i--) {
                        if (now - Integer.parseInt(signs.get(i).time) > 60 * 1000) {
                            signs.remove(i);
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private static String nonce() {
        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < 32; i2++) {
            stringBuffer.append("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(random.nextInt(62)));
        }
        return stringBuffer.toString();
    }
    public Sign get() {
        return signs.remove(0);
    }

}
