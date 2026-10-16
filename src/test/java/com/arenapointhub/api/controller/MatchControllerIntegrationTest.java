package com.arenapointhub.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class MatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAndRetrieveMatch() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Um", "jogador1@teste.com");
        Long player2Id = createPlayer("Jogador Dois", "jogador2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa 1",
                    "scheduledTime": "2026-10-13 14:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.player1.id").value(player1Id))
                .andExpect(jsonPath("$.player2.id").value(player2Id))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(get("/api/matches/{id}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId))
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.player1.id").value(player1Id))
                .andExpect(jsonPath("$.player2.id").value(player2Id));
    }

    @Test
    void shouldRejectMatchWithoutRequiredFields() throws Exception {
        mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private Long createTournament() throws Exception {
        String response = mockMvc.perform(
                post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Torneio Partidas"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private Long createCategory(Long tournamentId) throws Exception {
        String json = """
                {
                    "name": "Categoria Integração",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 18,
                    "setsToWinMatch": 2,
                    "tournamentId": %d
                }
                """.formatted(tournamentId);

        String response = mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private Long createPlayer(String name, String email) throws Exception {
        String json = """
                {
                    "name": "%s",
                    "email": "%s",
                    "birthDate": "%s"
                }
                """.formatted(name, email, LocalDate.of(2008, 5, 10));

        String response = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
