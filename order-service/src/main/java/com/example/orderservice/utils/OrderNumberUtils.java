package com.example.orderservice.utils;

import java.security.SecureRandom;

/**
 * 业务订单号生成
 */
public final class OrderNumberUtils {

    private static final char[] ALNUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private OrderNumberUtils() {
    }

    /**
     * 毫秒时间戳 + 用户ID + 4 位随机字母数字
     */
    public static String generate(long userId) {
        StringBuilder sb = new StringBuilder(40);
        sb.append(System.currentTimeMillis());
        sb.append(userId);
        for (int i = 0; i < 4; i++) {
            sb.append(ALNUM[RANDOM.nextInt(ALNUM.length)]);
        }
        return sb.toString();
    }
}
