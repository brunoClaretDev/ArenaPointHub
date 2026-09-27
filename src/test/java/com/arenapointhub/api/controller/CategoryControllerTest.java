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
import com.arenapointhub.api.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        categoryController = new CategoryController(categoryService);
    }

    @Test
    void shouldCreateCategory() {
        CategoryRequestDTO request = new CategoryRequestDTO();
        CategoryResponseDTO expected = new CategoryResponseDTO();

        when(categoryService.createCategory(request)).thenReturn(expected);

        ResponseEntity<CategoryResponseDTO> response =
                categoryController.createCategory(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).createCategory(request);
    }

    @Test
    void shouldGetAllCategories() {
        List<CategoryResponseDTO> expected =
                List.of(new CategoryResponseDTO());

        when(categoryService.getAllCategories()).thenReturn(expected);

        ResponseEntity<List<CategoryResponseDTO>> response =
                categoryController.getAllCategories();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getAllCategories();
    }

    @Test
    void shouldGetCategoryById() {
        Long categoryId = 1L;
        CategoryResponseDTO expected = new CategoryResponseDTO();

        when(categoryService.getCategoryById(categoryId)).thenReturn(expected);

        ResponseEntity<CategoryResponseDTO> response =
                categoryController.getCategoryById(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getCategoryById(categoryId);
    }

    @Test
    void shouldUpdateCategory() {
        Long categoryId = 1L;
        CategoryRequestDTO request = new CategoryRequestDTO();
        CategoryResponseDTO expected = new CategoryResponseDTO();

        when(categoryService.updateCategory(categoryId, request))
                .thenReturn(expected);

        ResponseEntity<CategoryResponseDTO> response =
                categoryController.updateCategory(categoryId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).updateCategory(categoryId, request);
    }

    @Test
    void shouldDeleteCategory() {
        Long categoryId = 1L;

        ResponseEntity<Void> response =
                categoryController.deleteCategory(categoryId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(categoryService).deleteCategory(categoryId);
    }

    @Test
    void shouldGeneratePlayoffs() {
        Long categoryId = 1L;

        ResponseEntity<Void> response =
                categoryController.generatePlayoffs(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(categoryService).generatePlayoffs(categoryId);
    }

    @Test
    void shouldGetMatchesByCategory() {
        Long categoryId = 1L;
        List<MatchResponseDTO> expected = List.of();

        when(categoryService.getMatchesByCategory(categoryId))
                .thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                categoryController.getMatchesByCategory(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getMatchesByCategory(categoryId);
    }

    @Test
    void shouldGetChampionWhenChampionExists() {
        Long categoryId = 1L;
        Player expected = new Player();

        when(categoryService.getChampionByCategoryId(categoryId))
                .thenReturn(expected);

        ResponseEntity<Player> response =
                categoryController.getChampion(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getChampionByCategoryId(categoryId);
    }

    @Test
    void shouldReturnNoContentWhenChampionDoesNotExist() {
        Long categoryId = 1L;

        when(categoryService.getChampionByCategoryId(categoryId))
                .thenReturn(null);

        ResponseEntity<Player> response =
                categoryController.getChampion(categoryId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(categoryService).getChampionByCategoryId(categoryId);
    }

    @Test
    void shouldGetPlayersByCategory() {
        Long categoryId = 1L;
        List<Player> expected = List.of(new Player());

        when(categoryService.getPlayersByCategory(categoryId))
                .thenReturn(expected);

        ResponseEntity<List<Player>> response =
                categoryController.getPlayersByCategory(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).getPlayersByCategory(categoryId);
    }

    @Test
    void shouldAddPlayerToCategory() {
        Long categoryId = 1L;
        Long playerId = 2L;

        ResponseEntity<Void> response =
                categoryController.addPlayerToCategory(categoryId, playerId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(categoryService).addPlayerToCategory(categoryId, playerId);
    }

    @Test
    void shouldRemovePlayerFromCategory() {
        Long categoryId = 1L;
        Long playerId = 2L;

        ResponseEntity<Void> response =
                categoryController.removePlayerFromCategory(categoryId, playerId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(categoryService).removePlayerFromCategory(categoryId, playerId);
    }

    @Test
    void shouldDrawPlayersByCategory() {
        Long categoryId = 1L;
        List<Player> expected = List.of(new Player());

        when(categoryService.drawPlayersByCategory(categoryId))
                .thenReturn(expected);

        ResponseEntity<List<Player>> response =
                categoryController.drawPlayersByCategory(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(categoryService).drawPlayersByCategory(categoryId);
    }
}