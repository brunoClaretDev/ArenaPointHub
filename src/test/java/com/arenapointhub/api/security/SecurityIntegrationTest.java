
package com.arenapointhub.api.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.arenapointhub.api.service.AdministratorDetailsService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SecurityIntegrationTest {

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @MockitoBean
    private AdministratorDetailsService administratorDetailsService;

    private UserDetails userDetails;

    SecurityIntegrationTest(
            MockMvc mockMvc,
            JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("admin")
                .password(passwordEncoder.encode("password"))
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
    

	@Test
	void shouldRejectLoginWithInvalidCredentials() throws Exception {
		  when(administratorDetailsService.loadUserByUsername("admin"))
	        .thenReturn(userDetails);
		
		mockMvc.perform(post("/auth/login")
	            .contentType("application/json")
	            .content("""
	                    {
	                        "username": "admin",
	                        "password": "senha-incorreta"
	                    }
	                    """))
	            .andDo(print());
	    
	  
	}


}
