package com.hackathon.databeats.churninsight.infra.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class NetworkUtils {
    private NetworkUtils() {}

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }

        String ip = request.getHeader("X-Forwarded-For");

        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}