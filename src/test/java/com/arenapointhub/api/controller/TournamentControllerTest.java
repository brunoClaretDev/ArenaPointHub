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

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.TournamentService;

@ExtendWith(MockitoExtension.class)
class TournamentControllerTest {

    private static final Long ADMINISTRATOR_ID = 10L;
    private static final Long TOURNAMENT_ID = 1L;

    @Mock
    private TournamentService tournamentService;

    private TournamentController tournamentController;
    private AdministratorPrincipal principal;

    @BeforeEach
    void setUp() {
        tournamentController = new TournamentController(tournamentService);

        principal = new AdministratorPrincipal(
                ADMINISTRATOR_ID,
                "admin",
                "password",
                true);
    }

    @Test
    void shouldCreateTournamentForAuthenticatedAdministrator() {
        TournamentRequestDTO request = new TournamentRequestDTO();
        TournamentResponseDTO expected = new TournamentResponseDTO();

        when(tournamentService.createTournament(request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<TournamentResponseDTO> response =
                tournamentController.createTournament(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService)
                .createTournament(request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetAllTournamentsForAuthenticatedAdministrator() {
        List<TournamentResponseDTO> expected =
                List.of(new TournamentResponseDTO());

        when(tournamentService.getAllTournaments(ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<TournamentResponseDTO>> response =
                tournamentController.getAllTournaments(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService).getAllTournaments(ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetTournamentByIdForAuthenticatedAdministrator() {
        TournamentResponseDTO expected = new TournamentResponseDTO();

        when(tournamentService.getTournamentById(
                TOURNAMENT_ID, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<TournamentResponseDTO> response =
                tournamentController.getTournamentById(
                        TOURNAMENT_ID, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService)
                .getTournamentById(TOURNAMENT_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldUpdateTournamentForAuthenticatedAdministrator() {
        TournamentRequestDTO request = new TournamentRequestDTO();
        TournamentResponseDTO expected = new TournamentResponseDTO();

        when(tournamentService.updateTournament(
                TOURNAMENT_ID, request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<TournamentResponseDTO> response =
                tournamentController.updateTournament(
                        TOURNAMENT_ID, request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService)
                .updateTournament(TOURNAMENT_ID, request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldDeleteTournamentForAuthenticatedAdministrator() {
        ResponseEntity<Void> response =
                tournamentController.deleteTournament(
                        TOURNAMENT_ID, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(tournamentService)
                .deleteTournament(TOURNAMENT_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGeneratePlayoffsForAuthenticatedAdministrator() {
        List<BracketResponseDTO> expected =
                List.of(new BracketResponseDTO());

        when(tournamentService.generatePlayoffs(
                TOURNAMENT_ID, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<BracketResponseDTO>> response =
                tournamentController.generatePlayoffs(
                        TOURNAMENT_ID, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService)
                .generatePlayoffs(TOURNAMENT_ID, ADMINISTRATOR_ID);
    }
}