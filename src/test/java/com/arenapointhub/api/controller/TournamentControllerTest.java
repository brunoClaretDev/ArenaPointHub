
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
import com.arenapointhub.api.service.TournamentService;

@ExtendWith(MockitoExtension.class)
class TournamentControllerTest {

    @Mock
    private TournamentService tournamentService;

    private TournamentController tournamentController;

    @BeforeEach
    void setUp() {
        tournamentController = new TournamentController(tournamentService);
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
    void shouldGeneratePlayoffs() {
        Long tournamentId = 1L;
        List<BracketResponseDTO> expected =
                List.of(new BracketResponseDTO());

        when(tournamentService.generatePlayoffs(tournamentId))
                .thenReturn(expected);

        ResponseEntity<List<BracketResponseDTO>> response =
                tournamentController.generatePlayoffs(tournamentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(tournamentService).generatePlayoffs(tournamentId);
    }
}
