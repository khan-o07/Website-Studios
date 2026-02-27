package com.websitestudios.security.auth;

import com.websitestudios.dto.response.WsApiResponseDTO;
import com.websitestudios.exception.AccountLockedException;
import com.websitestudios.exception.UnauthorizedException;
import com.websitestudios.security.jwt.JwtProperties;
import com.websitestudios.security.jwt.JwtTokenProvider;
import com.websitestudios.security.lockout.AccountLockoutService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints — login, refresh, logout.
 *
 * POST /api/v1/auth/login → Authenticate and get JWT pair
 * POST /api/v1/auth/refresh → Refresh access token
 * POST /api/v1/auth/logout → Invalidate refresh token (TODO)
 *
 * All endpoints are PUBLIC (no JWT required to access them).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AccountLockoutService accountLockoutService;

    public AuthenticationController(AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties,
            AccountLockoutService accountLockoutService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.accountLockoutService = accountLockoutService;
    }

    // ════════════════════════════════════════════════════════════════
    // LOGIN
    // ════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/auth/login
     *
     * Flow:
     * 1. Check if account is locked
     * 2. Authenticate with username/password
     * 3. On success: reset failed attempts, generate JWT pair
     * 4. On failure: record failed attempt, potentially lock account
     */
    @PostMapping("/login")
    public ResponseEntity<WsApiResponseDTO<AuthResponseDTO>> login(
            @Valid @RequestBody AuthRequestDTO loginRequest,
            HttpServletRequest request) {

        String username = loginRequest.getUsername();
        String clientIp = request.getRemoteAddr();

        log.info("Login attempt for user: {} from IP: {}", username, clientIp);

        // Step 1: Check if account is locked
        if (accountLockoutService.isAccountLocked(username)) {
            log.warn("Login attempt for locked account: {} from IP: {}", username, clientIp);
            accountLockoutService.recordFailedAttempt(username, clientIp, "ACCOUNT_LOCKED");
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts.",
                    accountLockoutService.getLockExpiresAt(username));
        }

        try {
            // Step 2: Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            loginRequest.getPassword()));

            // Step 3: Success — reset failed attempts
            accountLockoutService.resetFailedAttempts(username);
            accountLockoutService.recordSuccessfulLogin(username, clientIp);

            // Step 4: Generate JWT pair
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // Extract role for response
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_ADMIN")
                    .replace("ROLE_", "");

            AuthResponseDTO authResponse = new AuthResponseDTO(
                    accessToken,
                    refreshToken,
                    jwtProperties.getAccessTokenExpiryMs() / 1000, // Convert to seconds
                    jwtProperties.getRefreshTokenExpiryMs() / 1000,
                    username,
                    role);

            log.info("Login successful for user: {} with role: {}", username, role);

            return ResponseEntity.ok(
                    WsApiResponseDTO.success("Login successful", authResponse));

        } catch (BadCredentialsException e) {
            // Record failed attempt
            accountLockoutService.recordFailedAttempt(username, clientIp, "BAD_CREDENTIALS");
            log.warn("Failed login attempt for user: {} from IP: {} — bad credentials",
                    username, clientIp);
            throw new UnauthorizedException("Invalid username or password.");

        } catch (LockedException e) {
            accountLockoutService.recordFailedAttempt(username, clientIp, "ACCOUNT_LOCKED");
            log.warn("Login attempt for locked account: {} from IP: {}", username, clientIp);
            throw new AccountLockedException("Account is locked. Please contact an administrator.");

        } catch (DisabledException e) {
            accountLockoutService.recordFailedAttempt(username, clientIp, "ACCOUNT_DISABLED");
            log.warn("Login attempt for disabled account: {} from IP: {}", username, clientIp);
            throw new UnauthorizedException("Account is disabled. Please contact an administrator.");
        }
    }

    // ════════════════════════════════════════════════════════════════
    // REFRESH TOKEN
    // ════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/auth/refresh
     *
     * Flow:
     * 1. Validate the refresh token
     * 2. Ensure it's a REFRESH token (not ACCESS)
     * 3. Extract username and roles
     * 4. Generate new JWT pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<WsApiResponseDTO<AuthResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshRequest) {

        String refreshToken = refreshRequest.getRefreshToken();

        log.info("Token refresh request received");

        // Step 1: Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new UnauthorizedException("Invalid or expired refresh token. Please login again.");
        }

        // Step 2: Ensure it's a REFRESH token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("Access token provided instead of refresh token");
            throw new UnauthorizedException("Invalid token type. A refresh token is required.");
        }

        // Step 3: Extract user info
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String roles = jwtTokenProvider.getRolesFromToken(refreshToken);

        // Step 4: Generate new token pair
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(username, roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshTokenFromUsername(username, roles);

        String role = roles.replace("ROLE_", "");

        AuthResponseDTO authResponse = new AuthResponseDTO(
                newAccessToken,
                newRefreshToken,
                jwtProperties.getAccessTokenExpiryMs() / 1000,
                jwtProperties.getRefreshTokenExpiryMs() / 1000,
                username,
                role);

        log.info("Token refreshed successfully for user: {}", username);

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Token refreshed successfully", authResponse));
    }

    // ════════════════════════════════════════════════════════════════
    // LOGOUT (Placeholder — will be enhanced with token blacklist)
    // ════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/auth/logout
     *
     * Currently: Client-side only (discard tokens).
     * TODO: Implement server-side token blacklist using Redis.
     */
    @PostMapping("/logout")
    public ResponseEntity<WsApiResponseDTO<Void>> logout() {

        log.info("Logout request received");

        // With stateless JWT, the client simply discards the tokens.
        // Server-side blacklisting can be added with Redis in Phase 7.

        return ResponseEntity.ok(
                WsApiResponseDTO.success("Logged out successfully. Please discard your tokens."));
    }
}