package com.websitestudios.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;

import java.security.SecureRandom;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider — generates and validates JSON Web Tokens.
 *
 * Token structure:
 * Header: { "alg": "HS512", "typ": "JWT" }
 * Payload: {
 * "sub": "admin_username",
 * "roles": "ROLE_ADMIN",
 * "iss": "website-studios-api",
 * "iat": 1705312200,
 * "exp": 1705313100,
 * "type": "ACCESS" | "REFRESH"
 * }
 * Signature: HMACSHA512(header + "." + payload, secret)
 *
 * Security:
 * - HS512 algorithm (HMAC + SHA-512)
 * - Secret key must be at least 64 bytes (512 bits) for HS512
 * - Access tokens: short-lived (15 min)
 * - Refresh tokens: longer-lived (7 days)
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;
    private final boolean requireStrongKey;

    public JwtTokenProvider(JwtProperties jwtProperties, boolean requireStrongKey) {
        this.jwtProperties = jwtProperties;
        this.requireStrongKey = requireStrongKey;

        String secret = jwtProperties.getSecretKey();

        if (secret == null || secret.isBlank()) {
            if (requireStrongKey) {
                throw new IllegalStateException("JWT secret-key must be provided for production");
            }

            // Development fallback: generate ephemeral key (do NOT use in prod)
            byte[] generated = new byte[64];
            new SecureRandom().nextBytes(generated);
            SecretKey ephemeral = Keys.hmacShaKeyFor(generated);
            this.signingKey = ephemeral;
            log.warn("No JWT secret configured; using ephemeral dev key (not for production)");
            return;
        }

        final byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT secret-key is not valid Base64", ex);
        }

        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    "JWT secret-key is too short; require at least 64 bytes (512 bits) for HS512");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Token Provider initialized with issuer: {}", jwtProperties.getIssuer());
    }

    // ════════════════════════════════════════════════════════════════
    // GENERATE TOKENS
    // ════════════════════════════════════════════════════════════════

    /**
     * Generate an access token from an Authentication object.
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return generateToken(username, roles, TOKEN_TYPE_ACCESS,
                jwtProperties.getAccessTokenExpiryMs());
    }

    /**
     * Generate a refresh token from an Authentication object.
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return generateToken(username, roles, TOKEN_TYPE_REFRESH,
                jwtProperties.getRefreshTokenExpiryMs());
    }

    /**
     * Generate an access token from a username and roles string.
     * Used when refreshing tokens (no Authentication object available).
     */
    public String generateAccessTokenFromUsername(String username, String roles) {
        return generateToken(username, roles, TOKEN_TYPE_ACCESS,
                jwtProperties.getAccessTokenExpiryMs());
    }

    /**
     * Generate a refresh token from a username and roles string.
     */
    public String generateRefreshTokenFromUsername(String username, String roles) {
        return generateToken(username, roles, TOKEN_TYPE_REFRESH,
                jwtProperties.getRefreshTokenExpiryMs());
    }

    /**
     * Core token generation method.
     */
    private String generateToken(String username, String roles,
            String tokenType, long expiryMs) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMs);

        String token = Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TYPE, tokenType)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        log.debug("Generated {} token for user: {}, expires: {}", tokenType, username, expiryDate);

        return token;
    }

    // ════════════════════════════════════════════════════════════════
    // EXTRACT CLAIMS
    // ════════════════════════════════════════════════════════════════

    /**
     * Extract username (subject) from token.
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract roles claim from token.
     */
    public String getRolesFromToken(String token) {
        return parseClaims(token).get(CLAIM_ROLES, String.class);
    }

    /**
     * Extract token type (ACCESS or REFRESH) from token.
     */
    public String getTokenType(String token) {
        return parseClaims(token).get(CLAIM_TYPE, String.class);
    }

    /**
     * Extract expiration date from token.
     */
    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    // ════════════════════════════════════════════════════════════════
    // VALIDATE TOKEN
    // ════════════════════════════════════════════════════════════════

    /**
     * Validate a JWT token.
     *
     * Checks:
     * 1. Signature is valid (not tampered)
     * 2. Token is not expired
     * 3. Token is not malformed
     * 4. Token algorithm is supported
     * 5. Issuer matches
     *
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);

            // Verify issuer (defense-in-depth) — treat mismatch as a failure
            if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
                throw new JwtException("Invalid JWT issuer: " + claims.getIssuer());
            }

            // Expiration is enforced by parseClaims (ExpiredJwtException thrown)
            return true;

        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for user: {}", e.getClaims().getSubject());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Check if a token is an access token.
     */
    public boolean isAccessToken(String token) {
        try {
            return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        try {
            return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════

    /**
     * Parse and return all claims from a token.
     */
    private Claims parseClaims(String token) {
        try {
            // Use the builder API (parser() returns JwtParserBuilder in this version)
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            // Algorithm enforcement — only accept HS512
            String alg = jws.getHeader().getAlgorithm();
            if (!SignatureAlgorithm.HS512.getValue().equalsIgnoreCase(alg)) {
                throw new JwtException("Unexpected JWT algorithm: " + alg);
            }

            // Issuer check
            String tokenIss = jws.getBody().getIssuer();
            if (tokenIss == null || !jwtProperties.getIssuer().equals(tokenIss)) {
                throw new JwtException("Invalid JWT issuer: " + tokenIss);
            }

            return jws.getBody();
        } catch (ExpiredJwtException e) {
            throw e; // let caller handle expired tokens explicitly
        } catch (JwtException e) {
            throw e;
        }
    }

}