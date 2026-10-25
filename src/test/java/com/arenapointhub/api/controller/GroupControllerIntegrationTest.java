
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
class GroupControllerIntegrationTest {

    private final MockMvc mockMvc;

    GroupControllerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void shouldCreateAndRetrieveGroup() throws Exception {
        Long categoryId = createCategory("Categoria Grupo Consulta");

        String groupJson = """
                {
                    "name": "Grupo Integração",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        String response = mockMvc.perform(
                post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(groupJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grupo Integração"))
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long groupId = ((Number) JsonPath.read(response, "$.id"))
                .longValue();

        mockMvc.perform(get("/api/groups/{id}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.name").value("Grupo Integração"))
                .andExpect(jsonPath("$.categoryId").value(categoryId));
    }

    @Test
    @WithMockUser
    void shouldListGroupsByCategory() throws Exception {
        Long categoryId = createCategory("Categoria Grupo Listagem");

        String groupJson = """
                {
                    "name": "Grupo Listagem",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(groupJson))
                .andExpect(status().isCreated());

        mockMvc.perform(
                get("/api/groups/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'Grupo Listagem')]")
                        .isNotEmpty());
    }

    @Test
    @WithMockUser
    void shouldRejectGroupWithoutRequiredFields() throws Exception {
        mockMvc.perform(
                post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private Long createCategory(String categoryName) throws Exception {
        String tournamentJson = """
                {
                    "name": "Torneio %s"
                }
                """.formatted(categoryName);

        String tournamentResponse = mockMvc.perform(
                post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tournamentJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long tournamentId = ((Number) JsonPath.read(
                tournamentResponse, "$.id"))
                .longValue();

        String categoryJson = """
                {
                    "name": "%s",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 18,
                    "setsToWinMatch": 2,
                    "tournamentId": %d
                }
                """.formatted(categoryName, tournamentId);

        String categoryResponse = mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(categoryResponse, "$.id"))
                .longValue();
    }
}
