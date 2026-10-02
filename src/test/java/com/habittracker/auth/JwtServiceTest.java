package com.habittracker.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long-for-hmac";

    private final JwtService jwtService = new JwtService(SECRET, 7);

    @Test
    void generatesTokenThatValidatesBackToTheSameSubject() {
        String token = jwtService.generateToken("user@example.com");
        assertThat(jwtService.validateAndGetSubject(token)).contains("user@example.com");
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.generateToken("user@example.com");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");
        assertThat(jwtService.validateAndGetSubject(tampered)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant past = Instant.now().minusSeconds(60);
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();

        assertThat(jwtService.validateAndGetSubject(expiredToken)).isEmpty();
    }

    @Test
    void rejectsGarbageToken() {
        assertThat(jwtService.validateAndGetSubject("not-a-jwt")).isEmpty();
    }
}
