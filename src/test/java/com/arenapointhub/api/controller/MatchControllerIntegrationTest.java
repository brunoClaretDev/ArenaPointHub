package com.arenapointhub.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    

    @Test
    void shouldUpdateScoreOfMatchInProgress() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Placar Um", "placar1@teste.com");
        Long player2Id = createPlayer("Jogador Placar Dois", "placar2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Teste Placar",
                    "scheduledTime": "2026-10-13 16:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        String scoreJson = """
                {
                    "scorePlayer1": 1,
                    "scorePlayer2": 0,
                    "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(
                patch("/api/matches/{id}/score", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scoreJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId))
                .andExpect(jsonPath("$.scorePlayer1").value(1))
                .andExpect(jsonPath("$.scorePlayer2").value(0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
    

    @Test
    void shouldListAllMatches() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Listagem Um", "listagem1@teste.com");
        Long player2Id = createPlayer("Jogador Listagem Dois", "listagem2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Listagem",
                    "scheduledTime": "2026-10-13 18:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + matchId + ")]").isNotEmpty());
    }


    @Test
    void shouldListMatchesByCategory() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Categoria Um", "categoria1@teste.com");
        Long player2Id = createPlayer("Jogador Categoria Dois", "categoria2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Categoria",
                    "scheduledTime": "2026-10-13 20:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(get("/api/matches/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + matchId + ")]").isNotEmpty());
    }
    
    @Test
    void shouldListMatchesByStatus() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Status Um", "status1@teste.com");
        Long player2Id = createPlayer("Jogador Status Dois", "status2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Status",
                    "scheduledTime": "2026-10-13 22:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(get("/api/matches/status/{status}", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + matchId + ")]").isNotEmpty());
    }

    @Test
    void shouldListMatchesByPlayer() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Filtro Um", "filtro1@teste.com");
        Long player2Id = createPlayer("Jogador Filtro Dois", "filtro2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Filtro Jogador",
                    "scheduledTime": "2026-10-14 10:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(get("/api/matches/player/{playerId}", player1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + matchId + ")]").isNotEmpty());
    }

    @Test
    void shouldUpdateMatch() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Update Um", "update1@teste.com");
        Long player2Id = createPlayer("Jogador Update Dois", "update2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Update",
                    "scheduledTime": "2026-10-14 12:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        String updateJson = """
                {
                    "scorePlayer1": 1,
                    "scorePlayer2": 0,
                    "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(
                put("/api/matches/{id}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId))
                .andExpect(jsonPath("$.scorePlayer1").value(1))
                .andExpect(jsonPath("$.scorePlayer2").value(0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldRegisterMatchSets() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Set Um", "set1@teste.com");
        Long player2Id = createPlayer("Jogador Set Dois", "set2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Registro Sets",
                    "scheduledTime": "2026-10-14 14:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        String setsJson = """
                {
                    "sets": [
                        {
                            "setNumber": 1,
                            "scorePlayer1": 11,
                            "scorePlayer2": 5
                        }
                    ]
                }
                """;

        mockMvc.perform(
                put("/api/matches/{matchId}/sets", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(setsJson))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMatchSets() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador Consulta Set Um", "consultaset1@teste.com");
        Long player2Id = createPlayer("Jogador Consulta Set Dois", "consultaset2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa Consulta Sets",
                    "scheduledTime": "2026-10-14 16:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        String setsJson = """
                {
                    "sets": [
                        {
                            "setNumber": 1,
                            "scorePlayer1": 11,
                            "scorePlayer2": 5
                        }
                    ]
                }
                """;

        mockMvc.perform(
                put("/api/matches/{matchId}/sets", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(setsJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches/{matchId}/sets", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].setNumber").value(1))
                .andExpect(jsonPath("$[0].scorePlayer1").value(11))
                .andExpect(jsonPath("$[0].scorePlayer2").value(5));
    }

    @Test
    void shouldRegisterWalkover() throws Exception {
        Long tournamentId = createTournament();
        Long categoryId = createCategory(tournamentId);
        Long player1Id = createPlayer("Jogador WO Um", "wo1@teste.com");
        Long player2Id = createPlayer("Jogador WO Dois", "wo2@teste.com");

        String matchJson = """
                {
                    "categoryId": %d,
                    "player1Id": %d,
                    "player2Id": %d,
                    "tableOrCourt": "Mesa WO",
                    "scheduledTime": "2026-10-14 18:00"
                }
                """.formatted(categoryId, player1Id, player2Id);

        String response = mockMvc.perform(
                post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long matchId = ((Number) JsonPath.read(response, "$.id")).longValue();

        String woJson = """
                {
                    "winnerPlayerId": %d
                }
                """.formatted(player1Id);

        mockMvc.perform(
                put("/api/matches/{id}/wo", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(woJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId))
                .andExpect(jsonPath("$.scorePlayer1").value(2))
                .andExpect(jsonPath("$.scorePlayer2").value(0))
                .andExpect(jsonPath("$.status").value("WO"));
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
