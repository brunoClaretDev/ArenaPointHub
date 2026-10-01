package com.arenapointhub.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;
import com.arenapointhub.api.service.GlobalRankingService;
import com.arenapointhub.api.service.TournamentService;

@ExtendWith(MockitoExtension.class)
class TournamentControllerTest {

    @Mock
    private TournamentService tournamentService;

    @Mock
    private GlobalRankingService globalRankingService;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private TournamentController tournamentController;

    @BeforeEach
    void setUp() {
        tournamentController = new TournamentController(
                tournamentService,
                globalRankingService,
                tournamentRepository,
                playerRepository,
                categoryRepository
        );
    }

    @Test
    void shouldCreateTournament() {
        TournamentRequestDTO request = new TournamentRequestDTO();
        TournamentResponseDTO expected = new TournamentResponseDTO();

        when(tournamentService.createTournament(request))
                .thenReturn(expected);

        ResponseEntity<TournamentResponseDTO> response =
                tournamentController.createTournament(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService).createTournament(request);
    }

    @Test
    void shouldGetAllTournaments() {
        List<TournamentResponseDTO> expected =
                List.of(new TournamentResponseDTO());

        when(tournamentService.getAllTournaments())
                .thenReturn(expected);

        ResponseEntity<List<TournamentResponseDTO>> response =
                tournamentController.getAllTournaments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService).getAllTournaments();
    }

    @Test
    void shouldGetTournamentById() {
        Long tournamentId = 1L;
        TournamentResponseDTO expected = new TournamentResponseDTO();

        when(tournamentService.getTournamentById(tournamentId))
                .thenReturn(expected);

        ResponseEntity<TournamentResponseDTO> response =
                tournamentController.getTournamentById(tournamentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService).getTournamentById(tournamentId);
    }

    @Test
    void shouldUpdateTournament() {
        Long tournamentId = 1L;
        TournamentRequestDTO request = new TournamentRequestDTO();
        TournamentResponseDTO expected = new TournamentResponseDTO();

        when(tournamentService.updateTournament(tournamentId, request))
                .thenReturn(expected);

        ResponseEntity<TournamentResponseDTO> response =
                tournamentController.updateTournament(tournamentId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService).updateTournament(tournamentId, request);
    }

    @Test
    void shouldDeleteTournament() {
        Long tournamentId = 1L;

        ResponseEntity<Void> response =
                tournamentController.deleteTournament(tournamentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(tournamentService).deleteTournament(tournamentId);
    }

    @Test
    void shouldGeneratePlayoffsMessage() {
        Long tournamentId = 1L;

        ResponseEntity<String> response =
                tournamentController.generatePlayoffs(tournamentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "Playoffs gerados com sucesso para o torneio ID: 1",
                response.getBody()
        );
    }

    @Test
    void shouldDistributeTournamentPoints() {
        Long tournamentId = 1L;

        Tournament tournament = createTournament(tournamentId);
        Category category = createCategory(1L, tournament);

        Player firstPlayer = new Player();
        Player secondPlayer = new Player();

        when(tournamentRepository.findById(tournamentId))
                .thenReturn(Optional.of(tournament));

        when(categoryRepository.findByTournamentId(tournamentId))
                .thenReturn(List.of(category));

        when(playerRepository.findAll())
                .thenReturn(List.of(firstPlayer, secondPlayer));

        ResponseEntity<String> response =
                tournamentController.testDistributePoints(tournamentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "Pontos distribuídos com sucesso para o ranking global!",
                response.getBody()
        );

        verify(globalRankingService).distributeTournamentPoints(
                tournament,
                category,
                Map.of(firstPlayer, 1, secondPlayer, 2)
        );
    }

    @Test
    void shouldReturnBadRequestWhenTournamentHasNoCategories() {
        Long tournamentId = 1L;
        Tournament tournament = createTournament(tournamentId);

        when(tournamentRepository.findById(tournamentId))
                .thenReturn(Optional.of(tournament));

        when(categoryRepository.findByTournamentId(tournamentId))
                .thenReturn(List.of());

        ResponseEntity<String> response =
                tournamentController.testDistributePoints(tournamentId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "O torneio não possui categorias cadastradas.",
                response.getBody()
        );
    }

    @Test
    void shouldReturnBadRequestWhenThereAreFewerThanTwoPlayers() {
        Long tournamentId = 1L;

        Tournament tournament = createTournament(tournamentId);
        Category category = createCategory(1L, tournament);

        when(tournamentRepository.findById(tournamentId))
                .thenReturn(Optional.of(tournament));

        when(categoryRepository.findByTournamentId(tournamentId))
                .thenReturn(List.of(category));

        when(playerRepository.findAll())
                .thenReturn(List.of(new Player()));

        ResponseEntity<String> response =
                tournamentController.testDistributePoints(tournamentId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "O banco precisa ter pelo menos 2 jogadores cadastrados.",
                response.getBody()
        );
    }

    @Test
    void shouldThrowExceptionWhenTournamentDoesNotExist() {
        Long tournamentId = 99L;

        when(tournamentRepository.findById(tournamentId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tournamentController.testDistributePoints(tournamentId)
        );

        assertEquals(
                "Torneio não encontrado com ID: 99",
                exception.getMessage()
        );
    }

    private Tournament createTournament(Long id) {
        Tournament tournament = new Tournament();
        tournament.setId(id);
        tournament.setName("Tournament Test");
        return tournament;
    }

    private Category createCategory(Long id, Tournament tournament) {
        Category category = new Category();
        category.setId(id);
        category.setTournament(tournament);
        category.setName("Category Test");
        return category;
    }
}