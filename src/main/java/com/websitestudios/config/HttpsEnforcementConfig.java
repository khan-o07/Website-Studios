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
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

import java.io.IOException;

/**
 * Forces HTTPS in production environment.
 *
 * How it works:
 * 1. Checks X-Forwarded-Proto header (set by Nginx/load balancer)
 * 2. If request is HTTP → redirect to HTTPS (301 Permanent)
 * 3. Only active in "prod" profile — dev uses HTTP freely
 *
 * Nginx should also enforce HTTPS, this is defense-in-depth.
 */
@Configuration
@Profile("prod")
public class HttpsEnforcementConfig {

    private static final Logger log = LoggerFactory.getLogger(HttpsEnforcementConfig.class);

    @Value("${ws.security.https.enabled:true}")
    private boolean httpsEnabled;

    @Bean
    public FilterRegistrationBean<HttpsRedirectFilter> httpsRedirectFilter() {

        log.info("Registering HTTPS enforcement filter (prod profile)");

        FilterRegistrationBean<HttpsRedirectFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpsRedirectFilter(httpsEnabled));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.setName("httpsRedirectFilter");

        return registrationBean;
    }

    /**
     * Servlet filter that redirects HTTP requests to HTTPS.
     */
    static class HttpsRedirectFilter implements Filter {

        private static final Logger log = LoggerFactory.getLogger(HttpsRedirectFilter.class);

        private final boolean enabled;

        HttpsRedirectFilter(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {

            if (!enabled) {
                chain.doFilter(request, response);
                return;
            }

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Check if the request came via HTTPS
            // X-Forwarded-Proto is set by reverse proxies (Nginx, ALB, CloudFront)
            String forwardedProto = httpRequest.getHeader("X-Forwarded-Proto");
            boolean isSecure = httpRequest.isSecure()
                    || "https".equalsIgnoreCase(forwardedProto);

            if (!isSecure) {
                String redirectUrl = "https://" + httpRequest.getServerName()
                        + httpRequest.getRequestURI();

                String queryString = httpRequest.getQueryString();
                if (queryString != null) {
                    redirectUrl += "?" + queryString;
                }

                log.warn("HTTP request detected, redirecting to HTTPS: {}", redirectUrl);

                httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                httpResponse.setHeader("Location", redirectUrl);
                return;
            }

            chain.doFilter(request, response);
        }
    }
}