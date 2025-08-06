//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public final class NetworkUtil {
    public static final String getIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (log.isInfoEnabled()) {
            log.info("getIpAddress(HttpServletRequest) - X-Forwarded-For - String ip=" + ip);
        }

        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.length() > 15) {
                String[] ips = ip.split(",");

                for (int index = 0; index < ips.length; ++index) {
                    String strIp = ips[index];
                    if (!"unknown".equalsIgnoreCase(strIp)) {
                        ip = strIp;
                        break;
                    }
                }
            }
        } else {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
                if (log.isInfoEnabled()) {
                    log.info("getIpAddress(HttpServletRequest) - Proxy-Client-IP - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
                if (log.isInfoEnabled()) {
                    log.info("getIpAddress(HttpServletRequest) - WL-Proxy-Client-IP - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
                if (log.isInfoEnabled()) {
                    log.info("getIpAddress(HttpServletRequest) - HTTP_CLIENT_IP - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                if (log.isInfoEnabled()) {
                    log.info("getIpAddress(HttpServletRequest) - HTTP_X_FORWARDED_FOR - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (log.isInfoEnabled()) {
                    log.info("getIpAddress(HttpServletRequest) - getRemoteAddr - String ip=" + ip);
                }
            }
        }

        return ip;
    }
}
