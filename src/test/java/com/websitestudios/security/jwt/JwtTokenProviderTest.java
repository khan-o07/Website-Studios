package com.websitestudios.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtTokenProviderTest {

    @Test
    void validToken_parsesSuccessfully() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String secretB64 = Base64.getEncoder().encodeToString(key.getEncoded());

        JwtProperties props = new JwtProperties();
        props.setSecretKey(secretB64);
        props.setIssuer("test-issuer");

        JwtTokenProvider provider = new JwtTokenProvider(props, true);

        String token = provider.generateAccessTokenFromUsername("alice", "ROLE_USER");

        assertThat(provider.getUsernameFromToken(token)).isEqualTo("alice");
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void expiredToken_throwsExpiredJwtException() throws Exception {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String secretB64 = Base64.getEncoder().encodeToString(key.getEncoded());

        JwtProperties props = new JwtProperties();
        props.setSecretKey(secretB64);
        props.setIssuer("test-issuer");
        props.setAccessTokenExpiryMs(10); // 10 ms

        JwtTokenProvider provider = new JwtTokenProvider(props, true);

        String token = provider.generateAccessTokenFromUsername("bob", "ROLE_USER");

        Thread.sleep(20);

        assertThatThrownBy(() -> provider.getUsernameFromToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tamperedSignature_throwsSignatureException() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String secretB64 = Base64.getEncoder().encodeToString(key.getEncoded());

        JwtProperties props = new JwtProperties();
        props.setSecretKey(secretB64);
        props.setIssuer("test-issuer");

        JwtTokenProvider provider = new JwtTokenProvider(props, true);

        String token = provider.generateAccessTokenFromUsername("carol", "ROLE_USER");

        // Tamper the payload to break signature
        String[] segments = token.split("\\.");
        byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(segments[1]);
        // flip a byte
        payloadBytes[0] ^= 0x01;
        segments[1] = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payloadBytes);
        String tampered = String.join(".", segments);

        assertThatThrownBy(() -> provider.getUsernameFromToken(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void wrongIssuer_throwsJwtException() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String secretB64 = Base64.getEncoder().encodeToString(key.getEncoded());

        // Token built with different issuer
        JwtProperties tokenProps = new JwtProperties();
        tokenProps.setSecretKey(secretB64);
        tokenProps.setIssuer("other-issuer");
        JwtTokenProvider tokenProvider = new JwtTokenProvider(tokenProps, true);
        String token = tokenProvider.generateAccessTokenFromUsername("dave", "ROLE_USER");

        // Our validating provider expects 'test-issuer'
        JwtProperties validatingProps = new JwtProperties();
        validatingProps.setSecretKey(secretB64);
        validatingProps.setIssuer("test-issuer");
        JwtTokenProvider validatingProvider = new JwtTokenProvider(validatingProps, true);

        assertThatThrownBy(() -> validatingProvider.getUsernameFromToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void wrongAlgorithm_rejected() {
        // Create a base64 secret from an HS512 key
        SecretKey key512 = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String secretB64 = Base64.getEncoder().encodeToString(key512.getEncoded());

        // Build a token that uses HS256 header but signed with same bytes
        JwtProperties props = new JwtProperties();
        props.setSecretKey(secretB64);
        props.setIssuer("test-issuer");
        JwtTokenProvider provider = new JwtTokenProvider(props, true);

        // Manually build token with HS256 header
        SecretKey sameBytesKey = Keys.hmacShaKeyFor(key512.getEncoded());
        String badAlgToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("eve")
                .setIssuer("test-issuer")
                .signWith(sameBytesKey, SignatureAlgorithm.HS256)
                .compact();

        assertThatThrownBy(() -> provider.getUsernameFromToken(badAlgToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void secretTooShort_applicationFailsAtStartup() {
        // 16 bytes only
        byte[] shortKey = new byte[16];
        for (int i = 0; i < shortKey.length; i++)
            shortKey[i] = (byte) i;
        String shortB64 = Base64.getEncoder().encodeToString(shortKey);

        JwtProperties props = new JwtProperties();
        props.setSecretKey(shortB64);

        assertThatThrownBy(() -> new JwtTokenProvider(props, true)).isInstanceOf(IllegalStateException.class);
    }
}
