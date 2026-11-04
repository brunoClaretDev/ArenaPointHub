
package com.arenapointhub.api.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.repository.AdministratorRepository;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GroupControllerIntegrationTest {

	private MockMvc mockMvc;

	private final WebApplicationContext context;
	private final AdministratorRepository administratorRepository;

	GroupControllerIntegrationTest(
	        WebApplicationContext context,
	        AdministratorRepository administratorRepository) {

	    this.context = context;
	    this.administratorRepository = administratorRepository;
	}

	@BeforeEach
	void setUp() {

	    Administrator administrator = new Administrator();
	    administrator.setUsername("admin-test-" + UUID.randomUUID());
	    administrator.setPasswordHash("test-password");
	    administrator.setActive(true);

	    Administrator savedAdministrator =
	            administratorRepository.saveAndFlush(administrator);

	    AdministratorPrincipal principal = new AdministratorPrincipal(
	            savedAdministrator.getId(),
	            savedAdministrator.getUsername(),
	            savedAdministrator.getPasswordHash(),
	            savedAdministrator.isActive()
	    );

	    this.mockMvc = MockMvcBuilders
	            .webAppContextSetup(context)
	            .apply(springSecurity())
	            .defaultRequest(
	                    get("/").with(user(principal))
	            )
	            .build();
	}
	
    @Test
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
