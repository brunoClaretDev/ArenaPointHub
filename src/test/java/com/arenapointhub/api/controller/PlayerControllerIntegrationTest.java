
package com.arenapointhub.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PlayerControllerIntegrationTest {

    private final MockMvc mockMvc;

    PlayerControllerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void shouldCreateAndRetrievePlayer() throws Exception {
        String playerJson = """
                {
                    "name": "Jogador Integração",
                    "email": "jogador.integracao@teste.com",
                    "birthDate": "2000-05-10",
                    "phone": "11999999999",
                    "clubAcademy": "Clube Integração"
                }
                """;

        String response = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jogador Integração"))
                .andExpect(jsonPath("$.email")
                        .value("jogador.integracao@teste.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long playerId = ((Number) JsonPath.read(response, "$.id"))
                .longValue();

        mockMvc.perform(get("/api/players/{id}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playerId))
                .andExpect(jsonPath("$.name").value("Jogador Integração"))
                .andExpect(jsonPath("$.email")
                        .value("jogador.integracao@teste.com"));
    }

    @Test
    @WithMockUser
    void shouldListPlayers() throws Exception {
        String playerJson = """
                {
                    "name": "Jogador Listagem",
                    "email": "jogador.listagem@teste.com",
                    "birthDate": "2001-06-15"
                }
                """;

        mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'jogador.listagem@teste.com')]")
                        .isNotEmpty());
    }

    @Test
    @WithMockUser
    void shouldRejectPlayerWithoutRequiredFields() throws Exception {
        mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
