
package com.arenapointhub.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenapointhub.api.dto.CategoryRequestDTO;
import com.arenapointhub.api.dto.CategoryResponseDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private static final Long ADMINISTRATOR_ID = 1L;

    @Mock
    private CategoryService categoryService;

    private CategoryController categoryController;

    private AdministratorPrincipal principal;

    @BeforeEach
    void setUp() {
        categoryController = new CategoryController(categoryService);

        principal = new AdministratorPrincipal(
                ADMINISTRATOR_ID,
                "admin",
                "password",
                true);
    }

    @Test
    void shouldCreateCategory() {
        CategoryRequestDTO request = new CategoryRequestDTO();
        CategoryResponseDTO expected = new CategoryResponseDTO();

        when(categoryService.createCategory(request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<CategoryResponseDTO> response =
                categoryController.createCategory(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).createCategory(request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetAllCategories() {
        List<CategoryResponseDTO> expected =
                List.of(new CategoryResponseDTO());

        when(categoryService.getAllCategories(ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<CategoryResponseDTO>> response =
                categoryController.getAllCategories(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getAllCategories(ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetCategoryById() {
        Long categoryId = 1L;
        CategoryResponseDTO expected = new CategoryResponseDTO();

        when(categoryService.getCategoryById(categoryId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<CategoryResponseDTO> response =
                categoryController.getCategoryById(categoryId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getCategoryById(categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldUpdateCategory() {
        Long categoryId = 1L;
        CategoryRequestDTO request = new CategoryRequestDTO();
        CategoryResponseDTO expected = new CategoryResponseDTO();

        when(categoryService.updateCategory(
                categoryId, request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<CategoryResponseDTO> response =
                categoryController.updateCategory(categoryId, request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).updateCategory(
                categoryId, request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldDeleteCategory() {
        Long categoryId = 1L;

        ResponseEntity<Void> response =
                categoryController.deleteCategory(categoryId, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(categoryService).deleteCategory(categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGeneratePlayoffs() {
        Long categoryId = 1L;

        ResponseEntity<Void> response =
                categoryController.generatePlayoffs(categoryId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(categoryService).generatePlayoffs(categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetMatchesByCategory() {
        Long categoryId = 1L;
        List<MatchResponseDTO> expected = List.of();

        when(categoryService.getMatchesByCategory(categoryId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                categoryController.getMatchesByCategory(categoryId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getMatchesByCategory(
                categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetChampionWhenChampionExists() {
        Long categoryId = 1L;
        Player expected = new Player();

        when(categoryService.getChampionByCategoryId(
                categoryId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<Player> response =
                categoryController.getChampion(categoryId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getChampionByCategoryId(
                categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldReturnNoContentWhenChampionDoesNotExist() {
        Long categoryId = 1L;

        when(categoryService.getChampionByCategoryId(
                categoryId, ADMINISTRATOR_ID))
                .thenReturn(null);

        ResponseEntity<Player> response =
                categoryController.getChampion(categoryId, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(categoryService).getChampionByCategoryId(
                categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetPlayersByCategory() {
        Long categoryId = 1L;
        List<Player> expected = List.of(new Player());

        when(categoryService.getPlayersByCategory(
                categoryId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<Player>> response =
                categoryController.getPlayersByCategory(categoryId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getPlayersByCategory(
                categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldAddPlayerToCategory() {
        Long categoryId = 1L;
        Long playerId = 2L;

        ResponseEntity<Void> response =
                categoryController.addPlayerToCategory(
                        categoryId, playerId, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(categoryService).addPlayerToCategory(
                categoryId, playerId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldRemovePlayerFromCategory() {
        Long categoryId = 1L;
        Long playerId = 2L;

        ResponseEntity<Void> response =
                categoryController.removePlayerFromCategory(
                        categoryId, playerId, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(categoryService).removePlayerFromCategory(
                categoryId, playerId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldDrawPlayersByCategory() {
        Long categoryId = 1L;
        List<Player> expected = List.of(new Player());

        when(categoryService.drawPlayersByCategory(
                categoryId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<Player>> response =
                categoryController.drawPlayersByCategory(categoryId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).drawPlayersByCategory(
                categoryId, ADMINISTRATOR_ID);
    }
}
