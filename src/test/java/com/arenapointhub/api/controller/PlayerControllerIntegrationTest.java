
package com.arenapointhub.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    

	@Test
	@WithMockUser
	void shouldUpdatePlayer() throws Exception {
	    String playerJson = """
	            {
	                "name": "Jogador Antes",
	                "email": "jogador.antes@teste.com",
	                "birthDate": "2000-05-10"
	            }
	            """;
	
	    String response = mockMvc.perform(
	            post("/api/players")
	                    .contentType(MediaType.APPLICATION_JSON)
	                    .content(playerJson))
	            .andExpect(status().isCreated())
	            .andReturn()
	            .getResponse()
	            .getContentAsString();
	
	    Long playerId = ((Number) JsonPath.read(response, "$.id"))
	            .longValue();
	
	    String updatedPlayerJson = """
	            {
	                "name": "Jogador Atualizado",
	                "email": "jogador.atualizado@teste.com",
	                "birthDate": "2000-05-10",
	                "phone": "11988887777",
	                "clubAcademy": "Clube Atualizado"
	            }
	            """;
	
	    mockMvc.perform(
	            put("/api/players/{id}", playerId)
	                    .contentType(MediaType.APPLICATION_JSON)
	                    .content(updatedPlayerJson))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id").value(playerId))
	            .andExpect(jsonPath("$.name").value("Jogador Atualizado"))
	            .andExpect(jsonPath("$.email")
	                    .value("jogador.atualizado@teste.com"))
	            .andExpect(jsonPath("$.phone").value("11988887777"))
	            .andExpect(jsonPath("$.clubAcademy")
	                    .value("Clube Atualizado"));
	}

	@Test
	@WithMockUser
	void shouldDeletePlayer() throws Exception {
	    String playerJson = """
	            {
	                "name": "Jogador Para Excluir",
	                "email": "jogador.excluir@teste.com",
	                "birthDate": "2000-05-10"
	            }
	            """;
	
	    String response = mockMvc.perform(
	            post("/api/players")
	                    .contentType(MediaType.APPLICATION_JSON)
	                    .content(playerJson))
	            .andExpect(status().isCreated())
	            .andReturn()
	            .getResponse()
	            .getContentAsString();
	
	    Long playerId = ((Number) JsonPath.read(response, "$.id"))
	            .longValue();
	
	    mockMvc.perform(delete("/api/players/{id}", playerId))
	            .andExpect(status().isNoContent());
	
	    mockMvc.perform(get("/api/players/{id}", playerId))
	            .andExpect(status().isBadRequest());
	}

}
