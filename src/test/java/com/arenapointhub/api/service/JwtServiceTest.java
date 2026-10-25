
package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.arenapointhub.api.security.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(
                "ArenaPointHubTestSecretKeyForJwtTests123456".getBytes());

        jwtService = new JwtService(secret);

        userDetails = User.withUsername("admin")
                .password("password")
                .authorities("ROLE_ADMIN")
                .build();
    }

    @Test
    void shouldGenerateTokenWithUsername() {
        String token = jwtService.generateToken("admin");

        assertNotNull(token);
        assertEquals("admin", jwtService.extractUsername(token));
    }

    @Test
    void shouldValidateTokenForMatchingUser() {
        String token = jwtService.generateToken("admin");

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtService.generateToken("outro");

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThrows(
                io.jsonwebtoken.JwtException.class,
                () -> jwtService.extractUsername("token-invalido"));
    }
    
    @Test
    void shouldRejectExpiredToken() {
        String secret = Base64.getEncoder().encodeToString(
                "ArenaPointHubTestSecretKeyForJwtTests123456".getBytes());

        SecretKey secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret));

        Date now = new Date();
        Date expiration = new Date(now.getTime() - 1000);

        String expiredToken = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.extractUsername(expiredToken));
    }
}
