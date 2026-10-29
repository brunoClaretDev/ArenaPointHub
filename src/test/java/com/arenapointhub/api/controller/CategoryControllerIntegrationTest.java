package com.arenapointhub.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.web.servlet.MockMvc;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestConstructor(autowireMode = AutowireMode.ALL)
class CategoryControllerIntegrationTest {

	private final MockMvc mockMvc;
	private final CategoryRepository categoryRepository;
	private final PlayerRepository playerRepository;

	CategoryControllerIntegrationTest(
	        MockMvc mockMvc,
	        CategoryRepository categoryRepository,
	        PlayerRepository playerRepository) {

	    this.mockMvc = mockMvc;
	    this.categoryRepository = categoryRepository;
	    this.playerRepository = playerRepository;
	}
	
	@Test
	@WithMockUser
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
	
	    String categoryResponse = mockMvc.perform(
	            post("/api/categories")
	                    .contentType(MediaType.APPLICATION_JSON)
	                    .content(categoryJson))
	            .andExpect(status().isCreated())
	            .andExpect(content()
	                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
	            .andExpect(jsonPath("$.name").value("Sub-13"))
	            .andExpect(jsonPath("$.type").value("AGE"));
	}

    
    @Test
    @WithMockUser
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
    @WithMockUser
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

	@Test
	@WithMockUser
	void shouldListCategories() throws Exception {
	
	    String tournamentJson = """
	            {
	                "name": "Torneio Listagem Categorias"
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
	                "name": "Categoria Listagem",
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
	            .andExpect(status().isCreated());
	
	    mockMvc.perform(get("/api/categories"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath(
	                    "$[?(@.name == 'Categoria Listagem')]"
	            ).isNotEmpty());
	}
	
    @Test
    @WithMockUser
    void shouldUpdateCategory() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Atualização"
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
                tournamentResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Original",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 13,
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
                categoryResponse, "$.id")).longValue();

        String updatedCategoryJson = """
                {
                    "name": "Categoria Atualizada",
                    "type": "AGE",
                    "minAge": 14,
                    "maxAge": 16,
                    "setsToWinMatch": 3,
                    "tournamentId": %d
                }
                """.formatted(tournamentId);

        mockMvc.perform(
                put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedCategoryJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Categoria Atualizada"))
                .andExpect(jsonPath("$.minAge").value(14))
                .andExpect(jsonPath("$.maxAge").value(16))
                .andExpect(jsonPath("$.setsToWinMatch").value(3));
    }
    
    @Test
    @WithMockUser
    void shouldDeleteCategory() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Exclusão Categoria"
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
                tournamentResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Para Excluir",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 13,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void shouldReturnNoContentWhenCategoryHasNoChampion() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Sem Campeão"
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
                tournamentResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Sem Campeão",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 13,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(get("/api/categories/{id}/champion", categoryId))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @WithMockUser
    void shouldReturnChampionWhenCategoryHasChampion() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Com Campeão"
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
                tournamentResponse, "$.id")).longValue();

        String playerJson = """
                {
                    "name": "Jogador Campeão",
                    "email": "campeao.categoria@test.com",
                    "birthDate": "2000-05-15"
                }
                """;

        String playerResponse = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long playerId = ((Number) JsonPath.read(
                playerResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Com Campeão",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 30,
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
                categoryResponse, "$.id")).longValue();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow();

        Player champion = playerRepository.findById(playerId)
                .orElseThrow();

        category.setChampion(champion);
        categoryRepository.saveAndFlush(category);

        mockMvc.perform(get("/api/categories/{id}/champion", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playerId))
                .andExpect(jsonPath("$.name").value("Jogador Campeão"))
                .andExpect(jsonPath("$.email").value("campeao.categoria@test.com"));
    }
    
    @Test
    @WithMockUser
    void shouldReturnEmptyMatchListWhenCategoryHasNoMatches() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Consulta Partidas"
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
                tournamentResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Sem Partidas",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 13,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(get("/api/categories/{categoryId}/matches", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    void shouldRejectMatchListWhenCategoryDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/categories/{categoryId}/matches", 999999L))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void shouldReturnEmptyPlayerListWhenCategoryHasNoPlayers() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Consulta Jogadores"
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
                tournamentResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Sem Jogadores",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 13,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(get("/api/categories/{categoryId}/players", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    @WithMockUser
    void shouldReturnPlayersWhenCategoryHasRegisteredPlayers() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Jogadores Inscritos"
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
                tournamentResponse, "$.id")).longValue();

        String playerJson = """
                {
                    "name": "Jogador Inscrito",
                    "email": "jogador.inscrito@test.com",
                    "birthDate": "2000-05-15"
                }
                """;

        String playerResponse = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long playerId = ((Number) JsonPath.read(
                playerResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Com Jogadores",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 30,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(
                post("/api/categories/{categoryId}/players/{playerId}",
                        categoryId, playerId))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/categories/{categoryId}/players", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(playerId))
                .andExpect(jsonPath("$[0].name").value("Jogador Inscrito"));
    }
    
    @Test
    @WithMockUser
    void shouldRejectAddingPlayerAlreadyRegistered() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Inscrição Duplicada"
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
                tournamentResponse, "$.id")).longValue();

        String playerJson = """
                {
                    "name": "Jogador Inscrição Duplicada",
                    "email": "jogador.duplicado@test.com",
                    "birthDate": "2000-05-15"
                }
                """;

        String playerResponse = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long playerId = ((Number) JsonPath.read(
                playerResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Inscrição Duplicada",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 30,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(
                post("/api/categories/{categoryId}/players/{playerId}",
                        categoryId, playerId))
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/categories/{categoryId}/players/{playerId}",
                        categoryId, playerId))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void shouldRemovePlayerFromCategory() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Remoção Jogador"
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
                tournamentResponse, "$.id")).longValue();

        String playerJson = """
                {
                    "name": "Jogador Para Remover",
                    "email": "jogador.remover@test.com",
                    "birthDate": "2000-05-15"
                }
                """;

        String playerResponse = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long playerId = ((Number) JsonPath.read(
                playerResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Remoção Jogador",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 30,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(
                post("/api/categories/{categoryId}/players/{playerId}",
                        categoryId, playerId))
                .andExpect(status().isCreated());

        mockMvc.perform(
                delete("/api/categories/{categoryId}/players/{playerId}",
                        categoryId, playerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{categoryId}/players", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    @WithMockUser
    void shouldRejectRemovingPlayerNotRegisteredInCategory() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Remoção Não Inscrito"
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
                tournamentResponse, "$.id")).longValue();

        String uniqueEmail = "jogador.nao.inscrito."
                + java.util.UUID.randomUUID()
                + "@test.com";

        String playerJson = """
                {
                    "name": "Jogador Não Inscrito",
                    "email": "%s",
                    "birthDate": "2000-05-15"
                }
                """.formatted(uniqueEmail);

        String playerResponse = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long playerId = ((Number) JsonPath.read(
                playerResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Remoção Não Inscrito",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 30,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(
                delete("/api/categories/{categoryId}/players/{playerId}",
                        categoryId, playerId))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void shouldRejectGeneratingPlayoffsWhenCategoryDoesNotExist() throws Exception {

        mockMvc.perform(
                post("/api/categories/{categoryId}/generate-playoffs", 999999L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldRejectGeneratingPlayoffsWhenCategoryHasNoGroups() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Playoffs Sem Grupos"
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
                tournamentResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Sem Grupos",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 13,
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
                categoryResponse, "$.id")).longValue();

        mockMvc.perform(
                post("/api/categories/{categoryId}/generate-playoffs",
                        categoryId))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void shouldRejectGeneratingPlayoffsWhenNotEnoughQualifiedPlayers() throws Exception {

        String tournamentJson = """
                {
                    "name": "Torneio Playoffs Insuficientes"
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
                tournamentResponse, "$.id")).longValue();

        String categoryJson = """
                {
                    "name": "Categoria Playoffs Insuficientes",
                    "type": "AGE",
                    "minAge": 10,
                    "maxAge": 30,
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
                categoryResponse, "$.id")).longValue();

        String player1Json = """
                {
                    "name": "Jogador Playoff Um",
                    "email": "playoff.um@test.com",
                    "birthDate": "2000-05-15"
                }
                """;

        String player1Response = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(player1Json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long player1Id = ((Number) JsonPath.read(
                player1Response, "$.id")).longValue();

        String player2Json = """
                {
                    "name": "Jogador Playoff Dois",
                    "email": "playoff.dois@test.com",
                    "birthDate": "2000-06-20"
                }
                """;

        String player2Response = mockMvc.perform(
                post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(player2Json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long player2Id = ((Number) JsonPath.read(
                player2Response, "$.id")).longValue();

        String groupJson = """
                {
                    "name": "Grupo A",
                    "categoryId": %d,
                    "playerIds": [%d, %d]
                }
                """.formatted(categoryId, player1Id, player2Id);

        mockMvc.perform(
                post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(groupJson))
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/groups/category/{categoryId}/generate-matches", categoryId))
                .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/categories/{categoryId}/generate-playoffs", categoryId))
                .andExpect(status().isBadRequest());
    }
}