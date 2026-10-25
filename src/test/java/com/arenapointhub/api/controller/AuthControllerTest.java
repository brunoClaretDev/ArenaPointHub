
package com.arenapointhub.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.arenapointhub.api.security.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        objectMapper = new ObjectMapper();

        AuthController controller =
                new AuthController(authenticationManager, jwtService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() throws Exception {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin", null, List.of());

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken("admin"))
                .thenReturn("jwt-token-teste");

        Map<String, String> request = Map.of(
                "username", "admin",
                "password", "senha123");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.token").value("jwt-token-teste"));

        verify(jwtService).generateToken("admin");
    }

    @Test
    void shouldRejectInvalidCredentials() {
        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        Map<String, String> request = Map.of(
                "username", "admin",
                "password", "senha-errada");

        assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))));

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }


    @Test
    void shouldRejectBlankCredentials() throws Exception {
        Map<String, String> request = Map.of(
                "username", "",
                "password", "");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .status().isBadRequest());

        verifyNoInteractions(authenticationManager, jwtService);
    }
}
