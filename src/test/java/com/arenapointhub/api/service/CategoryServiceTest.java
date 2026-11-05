
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

    private static final Long ADMINISTRATOR_ID = 1L;
    private static final Long CATEGORY_ID = 1L;

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
        category.setId(CATEGORY_ID);
        category.setPlayers(new ArrayList<>());

        player = new Player();
        player.setId(1L);
        player.setName("Jogador 1");
    }

    @Test
    void shouldAddPlayerToCategory() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        assertDoesNotThrow(() ->
                categoryService.addPlayerToCategory(
                        CATEGORY_ID, 1L, ADMINISTRATOR_ID));

        verify(categoryRepository).save(category);

        assertTrue(category.getPlayers().contains(player));
    }

    @Test
    void shouldNotAddPlayerWhenAlreadyRegistered() {

        category.getPlayers().add(player);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));
        
        when(playerRepository.findById(1L))
        		.thenReturn(Optional.of(player));
        
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(
                        CATEGORY_ID, 1L, ADMINISTRATOR_ID));

        assertEquals(
                "O jogador já está inscrito na categoria.",
                exception.getMessage());
    }

    @Test
    void shouldNotAddPlayerWhenPlayerDoesNotExist() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(
                        CATEGORY_ID, 99L, ADMINISTRATOR_ID));

        assertEquals(
                "Jogador não encontrado com o ID: 99",
                exception.getMessage());
    }

    @Test
    void shouldNotAddPlayerWhenCategoryDoesNotExist() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        99L, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(
                        99L, 1L, ADMINISTRATOR_ID));

        assertEquals(
                "Categoria não encontrada com o ID: 99",
                exception.getMessage());
    }

    @Test
    void shouldNotAddPlayerWhenBirthDateIsMissing() {

        category.setType(CategoryType.AGE);
        category.setName("Sub 13");
        category.setMaxAge(13);

        player.setBirthDate(null);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(
                        CATEGORY_ID, 1L, ADMINISTRATOR_ID));

        assertEquals(
                "A data de nascimento do jogador é obrigatória para categorias restritas por idade.",
                exception.getMessage());
    }

    @Test
    void shouldNotAddPlayerWhenAgeExceedsMaximum() {

        category.setType(CategoryType.AGE);
        category.setName("Sub 13");
        category.setMaxAge(13);

        player.setBirthDate(
                LocalDate.of(LocalDate.now().getYear() - 14, 1, 1));

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.addPlayerToCategory(
                        CATEGORY_ID, 1L, ADMINISTRATOR_ID));

        assertTrue(exception.getMessage()
                .contains("excede a idade máxima de 13 anos"));
    }

    @Test
    void shouldAddPlayerWhenAgeIsExactlyMaximum() {

        category.setType(CategoryType.AGE);
        category.setName("Sub 13");
        category.setMaxAge(13);

        player.setBirthDate(
                LocalDate.of(LocalDate.now().getYear() - 13, 1, 1));

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        assertDoesNotThrow(() ->
                categoryService.addPlayerToCategory(
                        CATEGORY_ID, 1L, ADMINISTRATOR_ID));

        verify(categoryRepository).save(category);

        assertTrue(category.getPlayers().contains(player));
    }

    @Test
    void shouldReturnPlayersByCategory() {

        category.getPlayers().add(player);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        List<Player> players =
                categoryService.getPlayersByCategory(
                        CATEGORY_ID, ADMINISTRATOR_ID);

        assertTrue(players.contains(player));
    }

    @Test
    void shouldNotReturnPlayersWhenCategoryDoesNotExist() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        99L, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.getPlayersByCategory(
                        99L, ADMINISTRATOR_ID));

        assertEquals(
                "Categoria não encontrada com o ID: 99",
                exception.getMessage());
    }

    @Test
    void shouldRemovePlayerFromCategory() {

        category.getPlayers().add(player);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        assertDoesNotThrow(() ->
                categoryService.removePlayerFromCategory(
                        CATEGORY_ID, 1L, ADMINISTRATOR_ID));

        verify(categoryRepository).save(category);

        assertTrue(category.getPlayers().isEmpty());
    }

    @Test
    void shouldNotRemovePlayerWhenPlayerIsNotRegistered() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.removePlayerFromCategory(
                        CATEGORY_ID, 1L, ADMINISTRATOR_ID));

        assertEquals(
                "O jogador não está inscrito na categoria.",
                exception.getMessage());
    }

    @Test
    void shouldDrawPlayersByCategory() {

        Player player2 = new Player();
        player2.setId(2L);
        player2.setName("Jogador 2");

        category.getPlayers().add(player);
        category.getPlayers().add(player2);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        List<Player> players =
                categoryService.drawPlayersByCategory(
                        CATEGORY_ID, ADMINISTRATOR_ID);

        assertTrue(players.contains(player));
        assertTrue(players.contains(player2));
        assertEquals(2, players.size());
    }

    @Test
    void shouldNotDrawPlayersWhenCategoryHasNoPlayers() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.drawPlayersByCategory(
                        CATEGORY_ID, ADMINISTRATOR_ID));

        assertEquals(
                "Não existem jogadores inscritos na categoria.",
                exception.getMessage());
    }

    @Test
    void shouldNotDrawPlayersWhenCategoryDoesNotExist() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        99L, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.drawPlayersByCategory(
                        99L, ADMINISTRATOR_ID));

        assertEquals(
                "Categoria não encontrada com o ID: 99",
                exception.getMessage());
    }

    @Test
    void shouldReturnChampionByCategory() {

        category.setChampion(player);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        Player champion =
                categoryService.getChampionByCategoryId(
                        CATEGORY_ID, ADMINISTRATOR_ID);

        assertTrue(champion == player);
    }

    @Test
    void shouldNotReturnChampionWhenCategoryDoesNotExist() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        99L, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.getChampionByCategoryId(
                        99L, ADMINISTRATOR_ID));

        assertEquals(
                "Categoria não encontrada com o ID: 99",
                exception.getMessage());
    }

    @Test
    void shouldNotGeneratePlayoffsWhenCategoryDoesNotExist() {

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        99L, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.generatePlayoffs(
                        99L, ADMINISTRATOR_ID));

        assertEquals(
                "Categoria não encontrada com o ID: 99",
                exception.getMessage());
    }
}
