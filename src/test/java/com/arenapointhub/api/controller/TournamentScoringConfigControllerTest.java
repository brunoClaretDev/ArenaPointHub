package com.arenapointhub.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenapointhub.api.dto.TournamentScoringConfigDTO;
import com.arenapointhub.api.service.TournamentScoringConfigService;

@ExtendWith(MockitoExtension.class)
class TournamentScoringConfigControllerTest {

    @Mock
    private TournamentScoringConfigService configService;

    private TournamentScoringConfigController configController;

    @BeforeEach
    void setUp() {
        configController = new TournamentScoringConfigController(configService);
    }

    @Test
    void shouldSaveOrUpdateConfig() {
        TournamentScoringConfigDTO request = new TournamentScoringConfigDTO();
        TournamentScoringConfigDTO expected = new TournamentScoringConfigDTO();

        when(configService.saveOrUpdateConfig(request))
                .thenReturn(expected);

        ResponseEntity<TournamentScoringConfigDTO> response =
                configController.saveOrUpdateConfig(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(configService).saveOrUpdateConfig(request);
    }

    @Test
    void shouldGetConfigByTournament() {
        Long tournamentId = 1L;
        TournamentScoringConfigDTO expected =
                new TournamentScoringConfigDTO();

        when(configService.getConfigByTournament(tournamentId))
                .thenReturn(expected);

        ResponseEntity<TournamentScoringConfigDTO> response =
                configController.getConfigByTournament(tournamentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(configService).getConfigByTournament(tournamentId);
    }
}