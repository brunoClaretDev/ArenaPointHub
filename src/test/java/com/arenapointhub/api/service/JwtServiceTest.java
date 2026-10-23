
package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.arenapointhub.api.security.JwtService;

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
}
