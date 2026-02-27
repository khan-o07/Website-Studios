package com.websitestudios.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Filter — intercepts every request.
 *
 * Flow:
 * 1. Check if path is public (skip JWT check)
 * 2. Extract Bearer token from Authorization header
 * 3. Validate token (signature, expiry, issuer)
 * 4. Verify it's an ACCESS token (not REFRESH)
 * 5. Load user from database
 * 6. Set SecurityContext (user is now "authenticated")
 *
 * Extends OncePerRequestFilter to guarantee single execution per request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Public paths that skip JWT validation entirely.
     */
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/v1/health",
            "/api/v1/country-codes",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/health",
            "/actuator/info",
            "/error");

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    // ════════════════════════════════════════════════════════════════
    // SKIP PUBLIC PATHS
    // ════════════════════════════════════════════════════════════════

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Always allow OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // POST /api/v1/project-requests is public
        if ("POST".equalsIgnoreCase(method) && "/api/v1/project-requests".equals(path)) {
            return true;
        }

        // Check against public paths list
        return PUBLIC_PATHS.stream()
                .anyMatch(publicPath -> pathMatcher.match(publicPath, path));
    }

    // ════════════════════════════════════════════════════════════════
    // MAIN FILTER LOGIC
    // ════════════════════════════════════════════════════════════════

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from Authorization header
            String token = extractTokenFromRequest(request);

            if (token == null) {
                log.debug("No JWT token found in request to: {}", request.getServletPath());
                filterChain.doFilter(request, response);
                return;
            }

            // Step 2: Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid JWT token for request to: {}", request.getServletPath());
                filterChain.doFilter(request, response);
                return;
            }

            // Step 3: Ensure it's an ACCESS token (not REFRESH)
            if (!jwtTokenProvider.isAccessToken(token)) {
                log.warn("Refresh token used for API access — rejected. Path: {}",
                        request.getServletPath());
                filterChain.doFilter(request, response);
                return;
            }

            // Step 4: Extract username
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // Step 5: Load user details from database
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 6: Create authentication token and set in SecurityContext
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                log.debug("Authenticated user: {} with roles: {} for path: {}",
                        username, userDetails.getAuthorities(), request.getServletPath());
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Don't throw — let the request continue to get a proper 401 from Spring
            // Security
        }

        filterChain.doFilter(request, response);
    }

    // ════════════════════════════════════════════════════════════════
    // EXTRACT TOKEN
    // ════════════════════════════════════════════════════════════════

    /**
     * Extract Bearer token from the Authorization header.
     *
     * Expected header format: Authorization: Bearer <token>
     *
     * @return JWT token string, or null if not present/invalid
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}