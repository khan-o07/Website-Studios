package com.websitestudios.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility for extracting the real client IP address from HTTP requests.
 *
 * When behind a reverse proxy (Nginx, AWS ALB, CloudFront), the actual
 * client IP is forwarded via headers, not in request.getRemoteAddr().
 *
 * Header priority (checked in order):
 * 1. X-Forwarded-For → Most common proxy header
 * 2. X-Real-IP → Nginx header
 * 3. X-Original-Forwarded-For → AWS ALB header
 * 4. CF-Connecting-IP → Cloudflare header
 * 5. request.getRemoteAddr() → Fallback (direct connection)
 *
 * X-Forwarded-For format: "client, proxy1, proxy2"
 * We take the FIRST IP (leftmost) which is the original client.
 */
@Component
public class IpAddressUtil {

    private static final Logger log = LoggerFactory.getLogger(IpAddressUtil.class);

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "X-Original-Forwarded-For",
            "CF-Connecting-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    /**
     * Extract the real client IP from the request.
     *
     * @param request The HTTP servlet request
     * @return The client IP address string
     */
    public String extractClientIp(HttpServletRequest request) {

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);

            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                // X-Forwarded-For can contain multiple IPs — take the first one
                String ip = ipList.split(",")[0].trim();

                if (isValidIp(ip)) {
                    log.debug("Client IP extracted from header '{}': {}", header, ip);
                    return ip;
                }
            }
        }

        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        log.debug("Client IP from remoteAddr: {}", remoteAddr);
        return remoteAddr;
    }

    /**
     * Basic IP validation — checks it's not empty or "unknown".
     */
    private boolean isValidIp(String ip) {
        return ip != null
                && !ip.isEmpty()
                && !"unknown".equalsIgnoreCase(ip)
                && ip.length() <= 45; // Max IPv6 length
    }

    /**
     * Anonymize an IP for storage (GDPR compliance).
     * Removes last octet of IPv4: 192.168.1.100 → 192.168.1.0
     * For IPv6: removes last 80 bits.
     */
    public String anonymizeIp(String ip) {
        if (ip == null)
            return null;

        try {
            if (ip.contains(":")) {
                // IPv6 — zero out last 5 groups
                String[] parts = ip.split(":");
                if (parts.length >= 4) {
                    return parts[0] + ":" + parts[1] + ":" + parts[2] + ":" + parts[3] + ":0:0:0:0";
                }
            } else {
                // IPv4 — zero out last octet
                int lastDot = ip.lastIndexOf('.');
                if (lastDot > 0) {
                    return ip.substring(0, lastDot) + ".0";
                }
            }
        } catch (Exception e) {
            log.debug("Could not anonymize IP: {}", ip);
        }

        return ip;
    }
}