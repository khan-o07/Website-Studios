package com.websitestudios.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;

/**
 * Limits the maximum request body size.
 *
 * Protects against:
 * - Large payload denial-of-service (DoS)
 * - Memory exhaustion attacks
 * - Malicious oversized form submissions
 *
 * Default limit: 1MB (configurable via application.yml)
 *
 * Note: Spring Boot also has server.tomcat.max-http-form-post-size
 * and spring.servlet.multipart.max-file-size, but this filter
 * provides an additional application-level check.
 */
@Configuration
public class RequestSizeLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RequestSizeLimitConfig.class);

    @Value("${ws.security.max-request-size-bytes:1048576}") // 1MB default
    private long maxRequestSizeBytes;

    @Bean
    public FilterRegistrationBean<RequestSizeLimitFilter> requestSizeLimitFilter() {

        log.info("Registering request size limit filter: max {} bytes", maxRequestSizeBytes);

        FilterRegistrationBean<RequestSizeLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestSizeLimitFilter(maxRequestSizeBytes));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registrationBean.setName("requestSizeLimitFilter");

        return registrationBean;
    }

    /**
     * Servlet filter that rejects oversized request bodies.
     */
    static class RequestSizeLimitFilter implements Filter {

        private static final Logger log = LoggerFactory.getLogger(RequestSizeLimitFilter.class);

        private final long maxSizeBytes;

        RequestSizeLimitFilter(long maxSizeBytes) {
            this.maxSizeBytes = maxSizeBytes;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            long contentLength = httpRequest.getContentLengthLong();

            // Content-Length header check (if present)
            if (contentLength > maxSizeBytes) {
                log.warn("Request rejected — body too large: {} bytes (max: {}) from IP: {}",
                        contentLength, maxSizeBytes, httpRequest.getRemoteAddr());

                httpResponse.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                        "{" +
                                "\"status\":413," +
                                "\"error\":\"Payload Too Large\"," +
                                "\"message\":\"Request body exceeds the maximum allowed size of " +
                                (maxSizeBytes / 1024) + "KB.\"" +
                                "}");
                return;
            }

            chain.doFilter(request, response);
        }
    }
}