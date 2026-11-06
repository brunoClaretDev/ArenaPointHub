
package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.AdministratorRepository;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    private static final Long ADMINISTRATOR_ID = 10L;
    private static final Long OTHER_ADMINISTRATOR_ID = 20L;

    private static final Long TOURNAMENT_ID = 1L;
    private static final Long CATEGORY_ID = 10L;

    private static final String TOURNAMENT_NAME = "Tournament Test";

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BracketService bracketService;

    private TournamentService tournamentService;

    @BeforeEach
    void setUp() {
        tournamentService = new TournamentService(
                tournamentRepository,
                administratorRepository,
                categoryRepository,
                bracketService
        );
    }

    @Test
    void shouldCreateTournamentWithAdministratorAndProvidedStatus() {
        Administrator administrator = createAdministrator(ADMINISTRATOR_ID);

        TournamentRequestDTO dto = createTournamentRequest();
        dto.setStatus(TournamentStatus.IN_PROGRESS);

        when(administratorRepository.getReferenceById(ADMINISTRATOR_ID))
                .thenReturn(administrator);

        when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(invocation -> {
                    Tournament tournament = invocation.getArgument(0);
                    tournament.setId(TOURNAMENT_ID);
                    return tournament;
                });

        TournamentResponseDTO response = tournamentService.createTournament(
                dto, ADMINISTRATOR_ID);

        assertEquals(TOURNAMENT_ID, response.getId());
        assertEquals(TOURNAMENT_NAME, response.getName());
        assertEquals(TournamentStatus.IN_PROGRESS, response.getStatus());

        verify(administratorRepository)
                .getReferenceById(ADMINISTRATOR_ID);

        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void shouldCreateTournamentWithUpcomingStatusWhenStatusIsNull() {
        Administrator administrator = createAdministrator(ADMINISTRATOR_ID);

        TournamentRequestDTO dto = createTournamentRequest();
        dto.setStatus(null);

        when(administratorRepository.getReferenceById(ADMINISTRATOR_ID))
                .thenReturn(administrator);

        when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(invocation -> {
                    Tournament tournament = invocation.getArgument(0);
                    tournament.setId(TOURNAMENT_ID);
                    return tournament;
                });

        TournamentResponseDTO response = tournamentService.createTournament(
                dto, ADMINISTRATOR_ID);

        assertEquals(TournamentStatus.UPCOMING, response.getStatus());

        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void shouldReturnOnlyAdministratorTournaments() {
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

        when(tournamentRepository.findAllByAdministrator_Id(ADMINISTRATOR_ID))
                .thenReturn(List.of(tournament1, tournament2));

        List<TournamentResponseDTO> response =
                tournamentService.getAllTournaments(ADMINISTRATOR_ID);

        assertEquals(2, response.size());
        assertEquals("Tournament 1", response.get(0).getName());
        assertEquals("Tournament 2", response.get(1).getName());

        verify(tournamentRepository)
                .findAllByAdministrator_Id(ADMINISTRATOR_ID);

        verify(tournamentRepository, never()).findAll();
    }

    @Test
    void shouldReturnTournamentOwnedByAdministrator() {
        Tournament tournament = createTournament(
                TOURNAMENT_ID,
                TOURNAMENT_NAME,
                TournamentStatus.UPCOMING
        );

        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(tournament));

        TournamentResponseDTO response =
                tournamentService.getTournamentById(
                        TOURNAMENT_ID, ADMINISTRATOR_ID);

        assertEquals(TOURNAMENT_ID, response.getId());
        assertEquals(TOURNAMENT_NAME, response.getName());

        verify(tournamentRepository)
                .findByIdAndAdministrator_Id(
                        TOURNAMENT_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldNotReturnTournamentOwnedByAnotherAdministrator() {
        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> tournamentService.getTournamentById(
                        TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID)
        );

        verify(tournamentRepository)
                .findByIdAndAdministrator_Id(
                        TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID);
    }

    @Test
    void shouldUpdateTournamentOwnedByAdministrator() {
        Tournament tournament = createTournament(
                TOURNAMENT_ID,
                "Tournament Old",
                TournamentStatus.IN_PROGRESS
        );

        TournamentRequestDTO dto = createTournamentRequest();
        dto.setName("Tournament Updated");
        dto.setStatus(null);

        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(tournament));

        when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TournamentResponseDTO response =
                tournamentService.updateTournament(
                        TOURNAMENT_ID, dto, ADMINISTRATOR_ID);

        assertEquals("Tournament Updated", response.getName());
        assertEquals(TournamentStatus.IN_PROGRESS, response.getStatus());

        verify(tournamentRepository).save(tournament);
    }

    @Test
    void shouldNotUpdateTournamentOwnedByAnotherAdministrator() {
        TournamentRequestDTO dto = createTournamentRequest();

        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> tournamentService.updateTournament(
                        TOURNAMENT_ID, dto, OTHER_ADMINISTRATOR_ID)
        );

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void shouldDeleteTournamentOwnedByAdministrator() {
        Tournament tournament = createTournament(
                TOURNAMENT_ID,
                TOURNAMENT_NAME,
                TournamentStatus.UPCOMING
        );

        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(tournament));

        tournamentService.deleteTournament(
                TOURNAMENT_ID, ADMINISTRATOR_ID);

        verify(tournamentRepository)
                .findByIdAndAdministrator_Id(
                        TOURNAMENT_ID, ADMINISTRATOR_ID);

        verify(tournamentRepository).delete(tournament);
    }

    @Test
    void shouldNotDeleteTournamentOwnedByAnotherAdministrator() {
        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> tournamentService.deleteTournament(
                        TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID)
        );

        verify(tournamentRepository, never())
                .delete(any(Tournament.class));
    }

    @Test
    void shouldGeneratePlayoffsForOwnedTournament() {
        Tournament tournament = createTournament(
                TOURNAMENT_ID,
                TOURNAMENT_NAME,
                TournamentStatus.UPCOMING
        );

        Category category = new Category();
        category.setId(CATEGORY_ID);

        BracketResponseDTO bracket = new BracketResponseDTO();

        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(tournament));

        when(categoryRepository.findByTournamentId(TOURNAMENT_ID))
                .thenReturn(List.of(category));

        when(bracketService.generateKnockoutBracket(
                CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(bracket);

        List<BracketResponseDTO> response =
                tournamentService.generatePlayoffs(
                        TOURNAMENT_ID, ADMINISTRATOR_ID);

        assertEquals(1, response.size());
        assertSame(bracket, response.get(0));

        verify(bracketService)
                .generateKnockoutBracket(
                        CATEGORY_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldNotGeneratePlayoffsForTournamentOwnedByAnotherAdministrator() {
        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> tournamentService.generatePlayoffs(
                        TOURNAMENT_ID, OTHER_ADMINISTRATOR_ID)
        );

        verify(categoryRepository, never())
                .findByTournamentId(TOURNAMENT_ID);

        verify(bracketService, never())
                .generateKnockoutBracket(any(), any());
    }

    @Test
    void shouldNotGeneratePlayoffsWhenTournamentHasNoCategories() {
        Tournament tournament = createTournament(
                TOURNAMENT_ID,
                TOURNAMENT_NAME,
                TournamentStatus.UPCOMING
        );

        when(tournamentRepository.findByIdAndAdministrator_Id(
                TOURNAMENT_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(tournament));

        when(categoryRepository.findByTournamentId(TOURNAMENT_ID))
                .thenReturn(List.of());

        assertThrows(
                BusinessException.class,
                () -> tournamentService.generatePlayoffs(
                        TOURNAMENT_ID, ADMINISTRATOR_ID)
        );

        verify(bracketService, never())
                .generateKnockoutBracket(any(), any());
    }

    private Administrator createAdministrator(Long id) {
        Administrator administrator = new Administrator();
        administrator.setId(id);

        return administrator;
    }

    private TournamentRequestDTO createTournamentRequest() {
        TournamentRequestDTO dto = new TournamentRequestDTO();

        dto.setName(TOURNAMENT_NAME);
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
