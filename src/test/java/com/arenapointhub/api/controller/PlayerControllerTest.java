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

import com.arenapointhub.api.dto.PlayerRequestDTO;
import com.arenapointhub.api.dto.PlayerResponseDTO;
import com.arenapointhub.api.service.PlayerService;

@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    private PlayerController playerController;

    @BeforeEach
    void setUp() {
        playerController = new PlayerController(playerService);
    }

    @Test
    void shouldCreatePlayer() {
        PlayerRequestDTO request = new PlayerRequestDTO();
        PlayerResponseDTO expected = new PlayerResponseDTO();

        when(playerService.createPlayer(request)).thenReturn(expected);

        ResponseEntity<PlayerResponseDTO> response =
                playerController.createPlayer(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(playerService).createPlayer(request);
    }

    @Test
    void shouldGetAllPlayers() {
        List<PlayerResponseDTO> expected =
                List.of(new PlayerResponseDTO());

        when(playerService.getAllPlayers()).thenReturn(expected);

        ResponseEntity<List<PlayerResponseDTO>> response =
                playerController.getAllPlayers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(playerService).getAllPlayers();
    }

    @Test
    void shouldGetPlayerById() {
        Long playerId = 1L;
        PlayerResponseDTO expected = new PlayerResponseDTO();

        when(playerService.getPlayerById(playerId)).thenReturn(expected);

        ResponseEntity<PlayerResponseDTO> response =
                playerController.getPlayerById(playerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(playerService).getPlayerById(playerId);
    }

    @Test
    void shouldUpdatePlayer() {
        Long playerId = 1L;
        PlayerRequestDTO request = new PlayerRequestDTO();
        PlayerResponseDTO expected = new PlayerResponseDTO();

        when(playerService.updatePlayer(playerId, request))
                .thenReturn(expected);

        ResponseEntity<PlayerResponseDTO> response =
                playerController.updatePlayer(playerId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(playerService).updatePlayer(playerId, request);
    }

    @Test
    void shouldDeletePlayer() {
        Long playerId = 1L;

        ResponseEntity<Void> response =
                playerController.deletePlayer(playerId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(playerService).deletePlayer(playerId);
    }
}