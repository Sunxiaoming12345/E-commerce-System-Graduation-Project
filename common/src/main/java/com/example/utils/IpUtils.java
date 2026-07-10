package com.example.utils;

import javax.servlet.http.HttpServletRequest;

public class IpUtils {

    private IpUtils() {}

    /**
     * 获取客户端真实 IP，优先取代理转发的头
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isValid(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (isValid(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private static boolean isValid(String ip) {
        return ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip);
    }
}
