package com.websitestudios.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websitestudios.exception.WsErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point — handles 401 Unauthorized responses.
 *
 * Triggered when:
 * - No JWT token is provided for a protected endpoint
 * - JWT token is invalid, expired, or tampered
 * - Authentication fails for any reason
 *
 * Returns a clean JSON error response (no stack traces, no internal details).
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    private final ObjectMapper objectMapper;

    public JwtAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.warn("Unauthorized access attempt to: {} from IP: {} — {}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                authException.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication is required to access this resource. Please provide a valid JWT token.");
        errorResponse.setPath(request.getRequestURI());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}