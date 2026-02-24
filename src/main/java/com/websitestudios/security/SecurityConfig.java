package com.websitestudios.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Master Spring Security configuration.
 *
 * Phase 5: Sets up the security foundation:
 * - Stateless session (no cookies — preparing for JWT)
 * - CSRF disabled (stateless API — JWT will handle protection)
 * - Public vs Protected endpoint definitions
 * - BCrypt password encoder bean
 * - Security headers delegation
 *
 * Phase 6 will ADD:
 * - JwtAuthenticationFilter in the filter chain
 * - JwtAuthEntryPoint for 401 handling
 * - Full RBAC enforcement via @PreAuthorize
 *
 * IMPORTANT: Until Phase 6, admin endpoints are accessible without auth.
 * This is intentional — we need to test the API layer before locking it down.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final CorsConfig corsConfig;
    private final SecurityHeadersConfig securityHeadersConfig;

    public SecurityConfig(CorsConfig corsConfig,
            SecurityHeadersConfig securityHeadersConfig) {
        this.corsConfig = corsConfig;
        this.securityHeadersConfig = securityHeadersConfig;
    }

    // ════════════════════════════════════════════════════════════════
    // PUBLIC PATHS — No authentication required
    // ════════════════════════════════════════════════════════════════

    private static final String[] PUBLIC_GET_PATHS = {
            "/api/v1/health",
            "/api/v1/country-codes"
    };

    private static final String[] PUBLIC_POST_PATHS = {
            "/api/v1/project-requests"
            // Phase 6: "/api/v1/auth/login", "/api/v1/auth/refresh"
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

        log.info("Configuring Security Filter Chain");

        http
                // ──── CORS Configuration ────
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // ──── Disable CSRF (stateless API — no session cookies) ────
                .csrf(csrf -> csrf.disable())

                // ──── Stateless Session (no HttpSession, no cookies) ────
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ──── Security Headers ────
                .headers(headers -> securityHeadersConfig.configure(headers))

                // ──── Endpoint Authorization Rules ────
                .authorizeHttpRequests(auth -> auth

                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()

                        // Public POST endpoints
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()

                        // Swagger — allowed in dev, blocked in prod via ProfileBasedConfig
                        .requestMatchers(SWAGGER_PATHS).permitAll()

                        // Actuator — only health and info are public
                        .requestMatchers(ACTUATOR_PATHS).permitAll()

                        // ──── ADMIN ENDPOINTS ────
                        // Phase 6 will change these to .hasRole("ADMIN") / .hasRole("SUPER_ADMIN")
                        // For now: permit all to allow testing
                        .requestMatchers(HttpMethod.GET, "/api/v1/project-requests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/project-requests/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/project-requests/*/status").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/project-requests/*").permitAll()
                        .requestMatchers("/api/v1/admin/**").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated())

                // ──── Disable form login (REST API only) ────
                .formLogin(form -> form.disable())

                // ──── Disable HTTP Basic ────
                .httpBasic(basic -> basic.disable())

                // ──── Disable logout (stateless) ────
                .logout(logout -> logout.disable())

                // ──── Anonymous access for public endpoints ────
                .anonymous(anonymous -> anonymous.principal("anonymousUser"));

        // Phase 6: Add JWT filter here
        // http.addFilterBefore(jwtAuthenticationFilter,
        // UsernamePasswordAuthenticationFilter.class);

        log.info("Security Filter Chain configured successfully");

        return http.build();
    }

    // ════════════════════════════════════════════════════════════════
    // PASSWORD ENCODER
    // ════════════════════════════════════════════════════════════════

    /**
     * BCrypt password encoder with strength 12.
     * Used for admin password hashing.
     *
     * Strength 12 = ~250ms per hash (good balance of security vs performance).
     * Strength 10 = ~100ms (faster, less secure)
     * Strength 14 = ~1s (slower, more secure)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}