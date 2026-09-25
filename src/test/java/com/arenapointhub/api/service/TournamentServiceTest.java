package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.TournamentRepository;

class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    private TournamentService tournamentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tournamentService = new TournamentService(tournamentRepository);
    }

    @Test
    void shouldCreateTournamentWithProvidedStatus() {
        TournamentRequestDTO dto = createTournamentRequest();
        dto.setStatus(TournamentStatus.IN_PROGRESS);

        when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(invocation -> {
                    Tournament tournament = invocation.getArgument(0);
                    tournament.setId(1L);
                    return tournament;
                });

        TournamentResponseDTO response = tournamentService.createTournament(dto);

        assertEquals(1L, response.getId());
        assertEquals("Tournament Test", response.getName());
        assertEquals("Test tournament", response.getDescription());
        assertEquals("2026-10-10", response.getSchedule());
        assertEquals("ArenaPoint", response.getLocation());
        assertEquals(TournamentStatus.IN_PROGRESS, response.getStatus());

        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void shouldCreateTournamentWithUpcomingStatusWhenStatusIsNull() {
        TournamentRequestDTO dto = createTournamentRequest();
        dto.setStatus(null);

        when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(invocation -> {
                    Tournament tournament = invocation.getArgument(0);
                    tournament.setId(1L);
                    return tournament;
                });

        TournamentResponseDTO response = tournamentService.createTournament(dto);

        assertEquals(TournamentStatus.UPCOMING, response.getStatus());

        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void shouldReturnAllTournaments() {
        Tournament tournament1 = createTournament(
                1L,
                "Tournament 1",
                TournamentStatus.UPCOMING
        );

        Tournament tournament2 = createTournament(
                2L,
                "Tournament 2",
                TournamentStatus.IN_PROGRESS
        );

        when(tournamentRepository.findAll())
                .thenReturn(List.of(tournament1, tournament2));

        List<TournamentResponseDTO> response = tournamentService.getAllTournaments();

        assertEquals(2, response.size());
        assertEquals("Tournament 1", response.get(0).getName());
        assertEquals("Tournament 2", response.get(1).getName());
        assertEquals(TournamentStatus.UPCOMING, response.get(0).getStatus());
        assertEquals(TournamentStatus.IN_PROGRESS, response.get(1).getStatus());

        verify(tournamentRepository).findAll();
    }

    @Test
    void shouldReturnTournamentById() {
        Tournament tournament = createTournament(
                1L,
                "Tournament Test",
                TournamentStatus.UPCOMING
        );

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.of(tournament));

        TournamentResponseDTO response = tournamentService.getTournamentById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Tournament Test", response.getName());
        assertEquals(TournamentStatus.UPCOMING, response.getStatus());

        verify(tournamentRepository).findById(1L);
    }

    @Test
    void shouldNotReturnTournamentWhenIdDoesNotExist() {
        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tournamentService.getTournamentById(1L)
        );

        assertEquals(
                "Torneio não encontrado com o ID: 1",
                exception.getMessage()
        );

        verify(tournamentRepository).findById(1L);
    }

    @Test
    void shouldUpdateTournamentKeepingCurrentStatusWhenDtoStatusIsNull() {
        Tournament tournament = createTournament(
                1L,
                "Tournament Old",
                TournamentStatus.IN_PROGRESS
        );

        TournamentRequestDTO dto = createTournamentRequest();
        dto.setName("Tournament Updated");
        dto.setStatus(null);

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.of(tournament));

        when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TournamentResponseDTO response =
                tournamentService.updateTournament(1L, dto);

        assertEquals("Tournament Updated", response.getName());
        assertEquals(TournamentStatus.IN_PROGRESS, response.getStatus());

        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void shouldUpdateTournamentChangingStatus() {
        Tournament tournament = createTournament(
                1L,
                "Tournament Old",
                TournamentStatus.UPCOMING
        );

        TournamentRequestDTO dto = createTournamentRequest();
        dto.setName("Tournament Updated");
        dto.setStatus(TournamentStatus.FINISHED);

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.of(tournament));

        when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TournamentResponseDTO response =
                tournamentService.updateTournament(1L, dto);

        assertEquals("Tournament Updated", response.getName());
        assertEquals(TournamentStatus.FINISHED, response.getStatus());

        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void shouldNotUpdateTournamentWhenIdDoesNotExist() {
        TournamentRequestDTO dto = createTournamentRequest();

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tournamentService.updateTournament(1L, dto)
        );

        assertEquals(
                "Torneio não encontrado com o ID: 1",
                exception.getMessage()
        );

        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void shouldDeleteTournament() {
        when(tournamentRepository.existsById(1L))
                .thenReturn(true);

        tournamentService.deleteTournament(1L);

        verify(tournamentRepository).existsById(1L);
        verify(tournamentRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteTournamentWhenIdDoesNotExist() {
        when(tournamentRepository.existsById(1L))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tournamentService.deleteTournament(1L)
        );

        assertEquals(
                "Torneio não encontrado com o ID: 1",
                exception.getMessage()
        );

        verify(tournamentRepository).existsById(1L);
        verify(tournamentRepository, never()).deleteById(1L);
    }

    private TournamentRequestDTO createTournamentRequest() {
        TournamentRequestDTO dto = new TournamentRequestDTO();
        dto.setName("Tournament Test");
        dto.setDescription("Test tournament");
        dto.setSchedule("2026-10-10");
        dto.setLocation("ArenaPoint");
        return dto;
    }

    private Tournament createTournament(
            Long id,
            String name,
            TournamentStatus status) {

        Tournament tournament = new Tournament();
        tournament.setId(id);
        tournament.setName(name);
        tournament.setDescription("Test tournament");
        tournament.setSchedule("2026-10-10");
        tournament.setLocation("ArenaPoint");
        tournament.setStatus(status);

        return tournament;
    }
}