package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.PlayerRequestDTO;
import com.arenapointhub.api.dto.PlayerResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.repository.PlayerRepository;

class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        playerService = new PlayerService(playerRepository);
    }

    @Test
    void shouldCreatePlayer() {
        PlayerRequestDTO dto = createPlayerRequest();

        when(playerRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlayerResponseDTO response = playerService.createPlayer(dto);

        assertEquals(dto.getName(), response.getName());
        assertEquals(dto.getEmail(), response.getEmail());
        assertEquals(dto.getBirthDate(), response.getBirthDate());
        assertEquals(dto.getPhone(), response.getPhone());
        assertEquals(dto.getClubAcademy(), response.getClubAcademy());

        verify(playerRepository).existsByEmail(dto.getEmail());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void shouldNotCreatePlayerWhenEmailAlreadyExists() {
        PlayerRequestDTO dto = createPlayerRequest();

        when(playerRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.createPlayer(dto)
        );

        assertEquals(
                "Já existe um jogador cadastrado com o e-mail: " + dto.getEmail(),
                exception.getMessage()
        );

        verify(playerRepository).existsByEmail(dto.getEmail());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldReturnAllPlayers() {
        Player player1 = createPlayer(1L, "Player 1", "player1@email.com");
        Player player2 = createPlayer(2L, "Player 2", "player2@email.com");

        when(playerRepository.findAll()).thenReturn(List.of(player1, player2));

        List<PlayerResponseDTO> response = playerService.getAllPlayers();

        assertEquals(2, response.size());
        assertEquals("Player 1", response.get(0).getName());
        assertEquals("Player 2", response.get(1).getName());

        verify(playerRepository).findAll();
    }

    @Test
    void shouldReturnPlayerById() {
        Player player = createPlayer(1L, "Player 1", "player1@email.com");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        PlayerResponseDTO response = playerService.getPlayerById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Player 1", response.getName());
        assertEquals("player1@email.com", response.getEmail());

        verify(playerRepository).findById(1L);
    }

    @Test
    void shouldNotReturnPlayerWhenIdDoesNotExist() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.getPlayerById(1L)
        );

        assertEquals(
                "Jogador não encontrado com o ID: 1",
                exception.getMessage()
        );

        verify(playerRepository).findById(1L);
    }

    @Test
    void shouldUpdatePlayer() {
        Player player = createPlayer(1L, "Player 1", "player1@email.com");
        PlayerRequestDTO dto = createPlayerRequest();
        dto.setName("Player Updated");
        dto.setEmail("player1@email.com");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlayerResponseDTO response = playerService.updatePlayer(1L, dto);

        assertEquals("Player Updated", response.getName());
        assertEquals(dto.getEmail(), response.getEmail());
        assertEquals(dto.getBirthDate(), response.getBirthDate());
        assertEquals(dto.getPhone(), response.getPhone());
        assertEquals(dto.getClubAcademy(), response.getClubAcademy());

        verify(playerRepository).findById(1L);
        verify(playerRepository).save(player);
        verify(playerRepository, never()).existsByEmail(any(String.class));
    }

    @Test
    void shouldUpdatePlayerWhenEmailChangesToAvailableEmail() {
        Player player = createPlayer(1L, "Player 1", "player1@email.com");
        PlayerRequestDTO dto = createPlayerRequest();
        dto.setEmail("new@email.com");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlayerResponseDTO response = playerService.updatePlayer(1L, dto);

        assertEquals("new@email.com", response.getEmail());

        verify(playerRepository).findById(1L);
        verify(playerRepository).existsByEmail("new@email.com");
        verify(playerRepository).save(player);
    }

    @Test
    void shouldNotUpdatePlayerWhenNewEmailAlreadyExists() {
        Player player = createPlayer(1L, "Player 1", "player1@email.com");
        PlayerRequestDTO dto = createPlayerRequest();
        dto.setEmail("existing@email.com");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.existsByEmail("existing@email.com")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.updatePlayer(1L, dto)
        );

        assertEquals(
                "Já existe outro jogador cadastrado com o e-mail: existing@email.com",
                exception.getMessage()
        );

        verify(playerRepository).findById(1L);
        verify(playerRepository).existsByEmail("existing@email.com");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldNotUpdatePlayerWhenIdDoesNotExist() {
        PlayerRequestDTO dto = createPlayerRequest();

        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.updatePlayer(1L, dto)
        );

        assertEquals(
                "Jogador não encontrado com o ID: 1",
                exception.getMessage()
        );

        verify(playerRepository).findById(1L);
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldDeletePlayer() {
        when(playerRepository.existsById(1L)).thenReturn(true);

        playerService.deletePlayer(1L);

        verify(playerRepository).existsById(1L);
        verify(playerRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeletePlayerWhenIdDoesNotExist() {
        when(playerRepository.existsById(1L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.deletePlayer(1L)
        );

        assertEquals(
                "Jogador não encontrado com o ID: 1",
                exception.getMessage()
        );

        verify(playerRepository).existsById(1L);
        verify(playerRepository, never()).deleteById(1L);
    }

    private PlayerRequestDTO createPlayerRequest() {
        PlayerRequestDTO dto = new PlayerRequestDTO();
        dto.setName("Player Test");
        dto.setEmail("player@email.com");
        dto.setBirthDate(LocalDate.of(2012, 5, 10));
        dto.setPhone("11999999999");
        dto.setClubAcademy("ArenaPoint");
        return dto;
    }

    private Player createPlayer(Long id, String name, String email) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setEmail(email);
        player.setBirthDate(LocalDate.of(2012, 5, 10));
        player.setPhone("11999999999");
        player.setClubAcademy("ArenaPoint");
        return player;
    }
}