package com.websitestudios.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Request Logging Filter — logs every incoming request safely.
 *
 * Logged fields (MDC — Mapped Diagnostic Context):
 * requestId → Unique UUID per request (for log correlation)
 * method → HTTP method (GET, POST, etc.)
 * path → Request URI path
 * ip → Client IP address
 * duration → Request processing time in ms
 * status → HTTP response status code
 *
 * SECURITY — NEVER logged:
 * - Request bodies (may contain passwords, personal data)
 * - Authorization headers (contain JWT tokens)
 * - Query parameters on auth endpoints
 * - Email addresses or phone numbers
 * - Any PII (personally identifiable information)
 *
 * Log format (logback-spring.xml defines the output pattern):
 * [REQ:abc123] POST /api/v1/project-requests | IP: 1.2.3.4 | 45ms | 201
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_METHOD = "method";
    private static final String MDC_PATH = "path";
    private static final String MDC_IP = "clientIp";

    @Value("${ws.logging.log-requests:true}")
    private boolean logRequests;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Generate unique request ID for log correlation
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        String method = request.getMethod();
        String path = request.getServletPath();
        String ip = extractIp(request);

        // Skip logging for OPTIONS (CORS preflight) and health checks
        boolean shouldLog = logRequests
                && !"OPTIONS".equalsIgnoreCase(method)
                && !"/api/v1/health".equals(path)
                && !path.startsWith("/actuator");

        // Set MDC context for structured logging
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_METHOD, method);
        MDC.put(MDC_PATH, path);
        MDC.put(MDC_IP, ip);

        // Add request ID to response header for client-side correlation
        response.addHeader("X-Request-ID", requestId);

        try {
            if (shouldLog) {
                log.info("[REQ:{}] {} {} | IP: {}", requestId, method, path, ip);
            }

            filterChain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (shouldLog) {
                if (status >= 400) {
                    log.warn("[REQ:{}] {} {} | IP: {} | {}ms | STATUS: {}",
                            requestId, method, path, ip, duration, status);
                } else {
                    log.info("[REQ:{}] {} {} | IP: {} | {}ms | STATUS: {}",
                            requestId, method, path, ip, duration, status);
                }
            }

            // Clean up MDC to prevent thread-local leaks
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_METHOD);
            MDC.remove(MDC_PATH);
            MDC.remove(MDC_IP);
        }
    }

    private String extractIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}