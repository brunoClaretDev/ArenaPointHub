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

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.MatchScoreRequestDTO;
import com.arenapointhub.api.dto.MatchScoreUpdateDTO;
import com.arenapointhub.api.dto.MatchSetDTO;
import com.arenapointhub.api.dto.MatchWoRequestDTO;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.service.MatchService;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    @Mock
    private MatchService matchService;

    private MatchController matchController;

    @BeforeEach
    void setUp() {
        matchController = new MatchController(matchService);
    }

    @Test
    void shouldCreateMatch() {
        MatchRequestDTO request = new MatchRequestDTO();
        MatchResponseDTO expected = new MatchResponseDTO();

        when(matchService.createMatch(request)).thenReturn(expected);

        ResponseEntity<MatchResponseDTO> response =
                matchController.createMatch(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).createMatch(request);
    }

    @Test
    void shouldGetAllMatches() {
        List<MatchResponseDTO> expected = List.of(new MatchResponseDTO());

        when(matchService.getAllMatches()).thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                matchController.getAllMatches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).getAllMatches();
    }

    @Test
    void shouldGetMatchesByCategory() {
        Long categoryId = 1L;
        List<MatchResponseDTO> expected = List.of(new MatchResponseDTO());

        when(matchService.getMatchesByCategory(categoryId)).thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                matchController.getMatchesByCategory(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).getMatchesByCategory(categoryId);
    }

    @Test
    void shouldUpdateScore() {
        Long matchId = 1L;
        MatchScoreUpdateDTO request = new MatchScoreUpdateDTO();
        MatchResponseDTO expected = new MatchResponseDTO();

        when(matchService.updateScore(matchId, request)).thenReturn(expected);

        ResponseEntity<MatchResponseDTO> response =
                matchController.updateScore(matchId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).updateScore(matchId, request);
    }

    @Test
    void shouldGetMatchesByStatus() {
        MatchStatus status = MatchStatus.values()[0];
        List<MatchResponseDTO> expected = List.of(new MatchResponseDTO());

        when(matchService.getMatchesByStatus(status)).thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                matchController.getMatchesByStatus(status);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).getMatchesByStatus(status);
    }

    @Test
    void shouldGetMatchesByPlayer() {
        Long playerId = 1L;
        List<MatchResponseDTO> expected = List.of(new MatchResponseDTO());

        when(matchService.getMatchesByPlayer(playerId)).thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                matchController.getMatchesByPlayer(playerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).getMatchesByPlayer(playerId);
    }

    @Test
    void shouldUpdateMatch() {
        Long matchId = 1L;
        MatchRequestDTO request = new MatchRequestDTO();
        MatchResponseDTO expected = new MatchResponseDTO();

        when(matchService.updateMatch(matchId, request)).thenReturn(expected);

        ResponseEntity<MatchResponseDTO> response =
                matchController.updateMatch(matchId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).updateMatch(matchId, request);
    }

    @Test
    void shouldRegisterMatchSets() {
        Long matchId = 1L;
        MatchScoreRequestDTO request = new MatchScoreRequestDTO();

        ResponseEntity<Void> response =
                matchController.registerMatchSets(matchId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(matchService).saveMatchSets(matchId, request);
    }

    @Test
    void shouldGetMatchSets() {
        Long matchId = 1L;
        List<MatchSetDTO> expected = List.of(new MatchSetDTO());

        when(matchService.getSetsByMatchId(matchId)).thenReturn(expected);

        ResponseEntity<List<MatchSetDTO>> response =
                matchController.getMatchSets(matchId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).getSetsByMatchId(matchId);
    }

    @Test
    void shouldRegisterWalkover() {
        Long matchId = 1L;
        MatchWoRequestDTO request = new MatchWoRequestDTO();
        MatchResponseDTO expected = new MatchResponseDTO();

        when(matchService.registerWalkover(matchId, request))
                .thenReturn(expected);

        ResponseEntity<MatchResponseDTO> response =
                matchController.registerWalkover(matchId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).registerWalkover(matchId, request);
    }

    @Test
    void shouldGetMatchById() {
        Long matchId = 1L;
        MatchResponseDTO expected = new MatchResponseDTO();

        when(matchService.getMatchById(matchId)).thenReturn(expected);

        ResponseEntity<MatchResponseDTO> response =
                matchController.getMatchById(matchId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(matchService).getMatchById(matchId);
    }
}