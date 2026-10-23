
package com.arenapointhub.api.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.arenapointhub.api.service.AdministratorDetailsService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SecurityIntegrationTest {

    private final MockMvc mockMvc;
    private final JwtService jwtService;

    @MockitoBean
    private AdministratorDetailsService administratorDetailsService;

    private UserDetails userDetails;

    SecurityIntegrationTest(
            MockMvc mockMvc,
            JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
    }

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("admin")
                .password("password")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowRequestWithValidToken() throws Exception {
        when(administratorDetailsService.loadUserByUsername("admin"))
                .thenReturn(userDetails);

        String token = jwtService.generateToken("admin");

        mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
