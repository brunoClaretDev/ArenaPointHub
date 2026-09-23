package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.CategoryType;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Player player;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setPlayers(new ArrayList<>());

        player = new Player();
        player.setId(1L);
        player.setName("Jogador 1");
    }

    @Test
    void shouldAddPlayerToCategory() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        assertDoesNotThrow(() ->
                categoryService.addPlayerToCategory(1L, 1L));

        verify(categoryRepository).save(category);

        boolean playerRegistered = category.getPlayers()
                .stream()
                .anyMatch(p -> p.getId().equals(1L));

        assertTrue(playerRegistered);
    }

    @Test
    void shouldNotAddPlayerWhenAlreadyRegistered() {

        category.getPlayers().add(player);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(1L, 1L));

        assertTrue(
                exception.getMessage()
                        .equals("O jogador já está inscrito na categoria."));
    }

    @Test
    void shouldNotAddPlayerWhenPlayerDoesNotExist() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(1L, 99L));

        assertTrue(
                exception.getMessage()
                        .equals("Jogador não encontrado com ID: 99"));
    }

    @Test
    void shouldNotAddPlayerWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(99L, 1L));

        assertTrue(
                exception.getMessage()
                        .equals("Categoria não encontrada com ID: 99"));
    }

    @Test
    void shouldNotAddPlayerWhenBirthDateIsMissing() {

        category.setType(CategoryType.AGE);
        category.setName("Sub 13");
        category.setMaxAge(13);

        player.setBirthDate(null);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(1L, 1L));

        assertTrue(
                exception.getMessage()
                        .equals("A data de nascimento do jogador é obrigatória para categorias restritas por idade."));
    }

    @Test
    void shouldNotAddPlayerWhenAgeExceedsMaximum() {

        category.setType(CategoryType.AGE);
        category.setName("Sub 13");
        category.setMaxAge(13);

        player.setBirthDate(
                LocalDate.of(LocalDate.now().getYear() - 14, 1, 1));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(1L, 1L));

        assertTrue(
                exception.getMessage()
                        .contains("excede a idade máxima de 13 anos"));
    }

    @Test
    void shouldAddPlayerWhenAgeIsExactlyMaximum() {

        category.setType(CategoryType.AGE);
        category.setName("Sub 13");
        category.setMaxAge(13);

        player.setBirthDate(
                LocalDate.of(LocalDate.now().getYear() - 13, 1, 1));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        assertDoesNotThrow(() ->
                categoryService.addPlayerToCategory(1L, 1L));

        verify(categoryRepository).save(category);

        assertTrue(
                category.getPlayers()
                        .stream()
                        .anyMatch(p -> p.getId().equals(1L)));
    }
    
    @Test
    void shouldReturnPlayersByCategory() {

        category.getPlayers().add(player);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        List<Player> players =
                categoryService.getPlayersByCategory(1L);

        assertTrue(players.contains(player));
    }
    
    @Test
    void shouldNotReturnPlayersWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.getPlayersByCategory(99L));

        assertTrue(
                exception.getMessage()
                        .equals("Categoria não encontrada com o ID: 99"));
    }
    
    @Test
    void shouldRemovePlayerFromCategory() {

        category.getPlayers().add(player);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        assertDoesNotThrow(() ->
                categoryService.removePlayerFromCategory(1L, 1L));

        verify(categoryRepository).save(category);

        assertTrue(category.getPlayers().isEmpty());
    }
    
    @Test
    void shouldNotRemovePlayerWhenPlayerIsNotRegistered() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.removePlayerFromCategory(1L, 1L));

        assertTrue(
                exception.getMessage()
                        .equals("O jogador não está inscrito na categoria."));
    }
    
    @Test
    void shouldDrawPlayersByCategory() {

        Player player2 = new Player();
        player2.setId(2L);
        player2.setName("Jogador 2");

        category.getPlayers().add(player);
        category.getPlayers().add(player2);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        List<Player> players =
                categoryService.drawPlayersByCategory(1L);

        assertTrue(players.contains(player));
        assertTrue(players.contains(player2));
        assertTrue(players.size() == 2);
    }
    
    @Test
    void shouldNotDrawPlayersWhenCategoryHasNoPlayers() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.drawPlayersByCategory(1L));

        assertTrue(
                exception.getMessage()
                        .equals("Não existem jogadores inscritos na categoria."));
    }
    
    @Test
    void shouldNotDrawPlayersWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.drawPlayersByCategory(99L));

        assertTrue(
                exception.getMessage()
                        .equals("Categoria não encontrada com o ID: 99"));
    }
    
    @Test
    void shouldReturnChampionByCategory() {

        category.setChampion(player);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Player champion =
                categoryService.getChampionByCategoryId(1L);

        assertTrue(champion == player);
    }
    
    @Test
    void shouldNotReturnChampionWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.getChampionByCategoryId(99L));

        assertTrue(
                exception.getMessage()
                        .equals("Categoria não encontrada com o ID: 99"));
    }
    
    @Test
    void shouldNotGeneratePlayoffsWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.generatePlayoffs(99L));

        assertEquals(
                "Categoria não encontrada com o ID: 99",
                exception.getMessage());
    }
}