
package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;

@ExtendWith(MockitoExtension.class)
class GroupMatchServiceTest {

    private static final Long ADMINISTRATOR_ID = 100L;
    private static final Long GROUP_ID = 1L;
    private static final Long CATEGORY_ID = 1L;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private GroupMatchService groupMatchService;

    @BeforeEach
    void setUp() {
        // Os mocks e as dependências são inicializados pelo MockitoExtension.
    }

    @Test
    void shouldGenerateRoundRobinMatchesForGroup() {
        Group group = createGroupWithPlayers(GROUP_ID, CATEGORY_ID, 4);

        mockGroupLookup(group);
        mockGroupWithoutMatches();

        when(matchService.createMatch(
                any(MatchRequestDTO.class),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID)))
                .thenReturn(new MatchResponseDTO());

        List<MatchResponseDTO> response =
                groupMatchService.generateRoundRobinMatchesForGroup(
                        GROUP_ID,
                        "Mesa 1",
                        "10:00",
                        ADMINISTRATOR_ID);

        assertEquals(6, response.size());

        verify(matchService, times(6)).createMatch(
                any(MatchRequestDTO.class),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID));
    }

    @Test
    void shouldSetTableAndScheduledTimeForGeneratedMatches() {
        Group group = createGroupWithPlayers(GROUP_ID, CATEGORY_ID, 2);

        mockGroupLookup(group);
        mockGroupWithoutMatches();

        when(matchService.createMatch(
                any(MatchRequestDTO.class),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID)))
                .thenReturn(new MatchResponseDTO());

        groupMatchService.generateRoundRobinMatchesForGroup(
                GROUP_ID,
                "Mesa 2",
                "14:30",
                ADMINISTRATOR_ID);

        ArgumentCaptor<MatchRequestDTO> captor =
                ArgumentCaptor.forClass(MatchRequestDTO.class);

        verify(matchService).createMatch(
                captor.capture(),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID));

        MatchRequestDTO request = captor.getValue();

        assertEquals(CATEGORY_ID, request.getCategoryId());
        assertEquals(GROUP_ID, request.getGroupId());
        assertEquals("Mesa 2", request.getTableOrCourt());
        assertEquals("14:30", request.getScheduledTime());
    }

    @Test
    void shouldSetNullTableAndScheduledTimeWhenValuesAreNotProvided() {
        Group group = createGroupWithPlayers(GROUP_ID, CATEGORY_ID, 2);

        mockGroupLookup(group);
        mockGroupWithoutMatches();

        when(matchService.createMatch(
                any(MatchRequestDTO.class),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID)))
                .thenReturn(new MatchResponseDTO());

        groupMatchService.generateRoundRobinMatchesForGroup(
                GROUP_ID,
                "   ",
                "   ",
                ADMINISTRATOR_ID);

        ArgumentCaptor<MatchRequestDTO> captor =
                ArgumentCaptor.forClass(MatchRequestDTO.class);

        verify(matchService).createMatch(
                captor.capture(),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID));

        MatchRequestDTO request = captor.getValue();

        assertNull(request.getTableOrCourt());
        assertNull(request.getScheduledTime());
    }

    @Test
    void shouldNotGenerateMatchesWhenGroupHasLessThanTwoPlayers() {
        Group group = createGroupWithPlayers(GROUP_ID, CATEGORY_ID, 1);

        mockGroupLookup(group);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupMatchService.generateRoundRobinMatchesForGroup(
                        GROUP_ID,
                        "Mesa 1",
                        "10:00",
                        ADMINISTRATOR_ID));

        assertEquals(
                "O grupo precisa ter pelo menos 2 jogadores para gerar as partidas.",
                exception.getMessage());

        verify(matchRepository, never()).findByGroupId(GROUP_ID);

        verify(matchService, never()).createMatch(
                any(MatchRequestDTO.class),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID));
    }

    @Test
    void shouldNotGenerateMatchesWhenGroupAlreadyHasMatches() {
        Group group = createGroupWithPlayers(GROUP_ID, CATEGORY_ID, 2);

        mockGroupLookup(group);

        when(matchRepository.findByGroupId(GROUP_ID))
                .thenReturn(List.of(
                        new com.arenapointhub.api.model.Match()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupMatchService.generateRoundRobinMatchesForGroup(
                        GROUP_ID,
                        "Mesa 1",
                        "10:00",
                        ADMINISTRATOR_ID));

        assertEquals(
                "Já existem partidas geradas para este grupo.",
                exception.getMessage());

        verify(matchService, never()).createMatch(
                any(MatchRequestDTO.class),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID));
    }

    @Test
    void shouldNotGenerateMatchesWhenGroupDoesNotExist() {
        when(groupRepository.findByIdAndCategory_Tournament_Administrator_Id(
                GROUP_ID,
                ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupMatchService.generateRoundRobinMatchesForGroup(
                        GROUP_ID,
                        "Mesa 1",
                        "10:00",
                        ADMINISTRATOR_ID));

        assertEquals(
                "Grupo não encontrado com ID: 1",
                exception.getMessage());

        verify(matchRepository, never()).findByGroupId(GROUP_ID);

        verify(matchService, never()).createMatch(
                any(MatchRequestDTO.class),
                org.mockito.ArgumentMatchers.eq(ADMINISTRATOR_ID));
    }

    private void mockGroupLookup(Group group) {
        when(groupRepository.findByIdAndCategory_Tournament_Administrator_Id(
                GROUP_ID,
                ADMINISTRATOR_ID))
                .thenReturn(Optional.of(group));
    }

    private void mockGroupWithoutMatches() {
        when(matchRepository.findByGroupId(GROUP_ID))
                .thenReturn(List.of());
    }

    private Group createGroupWithPlayers(
            Long groupId,
            Long categoryId,
            int numberOfPlayers) {

        Group group = new Group();
        group.setId(groupId);
        group.setName("Grupo A");

        Administrator administrator = new Administrator();
        administrator.setId(ADMINISTRATOR_ID);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Tournament Test");
        tournament.setAdministrator(administrator);

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Sub 15");
        category.setTournament(tournament);

        group.setCategory(category);

        List<Player> players = new ArrayList<>();

        for (long i = 1; i <= numberOfPlayers; i++) {
            Player player = new Player();
            player.setId(i);
            player.setName("Player " + i);
            players.add(player);
        }

        group.setPlayers(players);

        return group;
    }
}
