package com.websitestudios.security;

import com.websitestudios.security.jwt.JwtAuthEntryPoint;
import com.websitestudios.security.jwt.JwtAuthenticationFilter;
import com.websitestudios.ratelimit.RateLimitFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Master Spring Security configuration.
 *
 * UPDATED in Phase 6:
 * - JWT Authentication Filter added to filter chain
 * - JWT Auth Entry Point for 401 responses
 * - AuthenticationManager bean exposed
 * - Admin endpoints now require authentication + roles
 * - @PreAuthorize enabled via @EnableMethodSecurity
 *
 * UPDATED:
 * - RateLimitFilter added before UsernamePasswordAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

        private final CorsConfig corsConfig;
        private final SecurityHeadersConfig securityHeadersConfig;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtAuthEntryPoint jwtAuthEntryPoint;
        private final RateLimitFilter rateLimitFilter;

        public SecurityConfig(CorsConfig corsConfig,
                        SecurityHeadersConfig securityHeadersConfig,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        JwtAuthEntryPoint jwtAuthEntryPoint,
                        RateLimitFilter rateLimitFilter) {

                this.corsConfig = corsConfig;
                this.securityHeadersConfig = securityHeadersConfig;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.jwtAuthEntryPoint = jwtAuthEntryPoint;
                this.rateLimitFilter = rateLimitFilter;
        }

        // ════════════════════════════════════════════════════════════════
        // PUBLIC PATHS
        // ════════════════════════════════════════════════════════════════

        private static final String[] PUBLIC_GET_PATHS = {
                        "/api/v1/health",
                        "/api/v1/country-codes"
        };

        private static final String[] PUBLIC_POST_PATHS = {
                        "/api/v1/project-requests",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout"
        };

        private static final String[] SWAGGER_PATHS = {
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
        };

        private static final String[] ACTUATOR_PATHS = {
                        "/actuator/health",
                        "/actuator/info"
        };

        // ════════════════════════════════════════════════════════════════
        // SECURITY FILTER CHAIN
        // ════════════════════════════════════════════════════════════════

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                log.info("Configuring Security Filter Chain with JWT Authentication + Rate Limiting");

                http
                                // ──── CORS ────
                                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                                // ──── CSRF disabled (stateless JWT) ────
                                .csrf(csrf -> csrf.disable())

                                // ──── Stateless sessions ────
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // ──── Security Headers ────
                                .headers(headers -> securityHeadersConfig.configure(headers))

                                // ──── Exception handling ────
                                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))

                                // ──── Authorization Rules ────
                                .authorizeHttpRequests(auth -> auth

                                                // Public GET
                                                .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()

                                                // Public POST
                                                .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()

                                                // Swagger
                                                .requestMatchers(SWAGGER_PATHS).permitAll()

                                                // Actuator
                                                .requestMatchers(ACTUATOR_PATHS).permitAll()

                                                // ──── ADMIN ENDPOINTS (ROLE-PROTECTED) ────

                                                .requestMatchers(HttpMethod.GET, "/api/v1/project-requests")
                                                .hasAnyRole("ADMIN", "SUPER_ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/project-requests/**")
                                                .hasAnyRole("ADMIN", "SUPER_ADMIN")

                                                .requestMatchers(HttpMethod.PUT, "/api/v1/project-requests/*/status")
                                                .hasAnyRole("ADMIN", "SUPER_ADMIN")

                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/project-requests/*")
                                                .hasRole("SUPER_ADMIN")

                                                .requestMatchers(HttpMethod.GET, "/api/v1/admin/dashboard")
                                                .hasAnyRole("ADMIN", "SUPER_ADMIN")

                                                .requestMatchers(HttpMethod.GET, "/api/v1/admin/audit-trail")
                                                .hasRole("SUPER_ADMIN")

                                                .requestMatchers(HttpMethod.GET, "/api/v1/admin/export")
                                                .hasRole("SUPER_ADMIN")

                                                // Everything else requires authentication
                                                .anyRequest().authenticated())

                                // ──── Disable form login, HTTP Basic, logout ────
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())
                                .logout(logout -> logout.disable())

                                // ──── Add RateLimit + JWT filters BEFORE UsernamePasswordAuthenticationFilter
                                // ────
                                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                log.info("Security Filter Chain configured with Rate Limiting + JWT + RBAC");

                return http.build();
        }

        // ════════════════════════════════════════════════════════════════
        // AUTHENTICATION MANAGER
        // ════════════════════════════════════════════════════════════════

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authenticationConfiguration) throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        // ════════════════════════════════════════════════════════════════
        // PASSWORD ENCODER
        // ════════════════════════════════════════════════════════════════

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }
}