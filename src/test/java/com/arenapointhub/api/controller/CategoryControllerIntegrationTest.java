package com.arenapointhub.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAndPersistCategory() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Integração"
                }
                """;

        String tournamentResponse = mockMvc.perform(
                post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tournamentJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long tournamentId = ((Number) JsonPath.read(
                tournamentResponse,
                "$.id"
        )).longValue();

        String categoryJson = """
                {
                    "name": "Sub-13",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 13,
                    "setsToWinMatch": 2,
                    "tournamentId": %d
                }
                """.formatted(tournamentId);

        mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Sub-13"));
    }
    
    @Test
    void shouldRejectCategoryWithoutRequiredFields() throws Exception {

        String invalidCategoryJson = """
                {
                    "description": "Categoria inválida"
                }
                """;

        mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCategoryJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldGetCategoryById() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Consulta"
                }
                """;

        String tournamentResponse = mockMvc.perform(
                post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tournamentJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long tournamentId = ((Number) JsonPath.read(
                tournamentResponse,
                "$.id"
        )).longValue();

        String categoryJson = """
                {
                    "name": "Sub-15",
                    "type": "AGE",
                    "minAge": 14,
                    "maxAge": 15,
                    "setsToWinMatch": 2,
                    "tournamentId": %d
                }
                """.formatted(tournamentId);

        String categoryResponse = mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = ((Number) JsonPath.read(
                categoryResponse,
                "$.id"
        )).longValue();

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Sub-15"))
                .andExpect(jsonPath("$.type").value("AGE"));
    }
}