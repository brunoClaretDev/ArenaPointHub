package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
}