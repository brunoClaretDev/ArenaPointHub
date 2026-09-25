package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;

class GroupMatchServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchService matchService;

    private GroupMatchService groupMatchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        groupMatchService = new GroupMatchService(
                groupRepository,
                matchRepository,
                matchService
        );
    }

    @Test
    void shouldGenerateRoundRobinMatchesForGroup() {
        Group group = createGroupWithPlayers(1L, 1L, 4);

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByGroupId(1L))
                .thenReturn(List.of());

        when(matchService.createMatch(any(MatchRequestDTO.class)))
                .thenReturn(new MatchResponseDTO());

        List<MatchResponseDTO> response =
                groupMatchService.generateRoundRobinMatchesForGroup(
                        1L,
                        "Mesa 1",
                        "10:00"
                );

        assertEquals(6, response.size());

        verify(matchService, org.mockito.Mockito.times(6))
                .createMatch(any(MatchRequestDTO.class));
    }

    @Test
    void shouldSetTableAndScheduledTimeForGeneratedMatches() {
        Group group = createGroupWithPlayers(1L, 1L, 2);

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByGroupId(1L))
                .thenReturn(List.of());

        when(matchService.createMatch(any(MatchRequestDTO.class)))
                .thenReturn(new MatchResponseDTO());

        groupMatchService.generateRoundRobinMatchesForGroup(
                1L,
                "Mesa 2",
                "14:30"
        );

        ArgumentCaptor<MatchRequestDTO> captor =
                ArgumentCaptor.forClass(MatchRequestDTO.class);

        verify(matchService).createMatch(captor.capture());

        MatchRequestDTO request = captor.getValue();

        assertEquals(1L, request.getCategoryId());
        assertEquals(1L, request.getGroupId());
        assertEquals("Mesa 2", request.getTableOrCourt());
        assertEquals("14:30", request.getScheduledTime());
    }

    @Test
    void shouldSetNullTableAndScheduledTimeWhenValuesAreNotProvided() {
        Group group = createGroupWithPlayers(1L, 1L, 2);

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByGroupId(1L))
                .thenReturn(List.of());

        when(matchService.createMatch(any(MatchRequestDTO.class)))
                .thenReturn(new MatchResponseDTO());

        groupMatchService.generateRoundRobinMatchesForGroup(
                1L,
                "   ",
                "   "
        );

        ArgumentCaptor<MatchRequestDTO> captor =
                ArgumentCaptor.forClass(MatchRequestDTO.class);

        verify(matchService).createMatch(captor.capture());

        MatchRequestDTO request = captor.getValue();

        assertNull(request.getTableOrCourt());
        assertNull(request.getScheduledTime());
    }

    @Test
    void shouldNotGenerateMatchesWhenGroupHasLessThanTwoPlayers() {
        Group group = createGroupWithPlayers(1L, 1L, 1);

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupMatchService.generateRoundRobinMatchesForGroup(
                        1L,
                        "Mesa 1",
                        "10:00"
                )
        );

        assertEquals(
                "O grupo precisa ter pelo menos 2 jogadores para gerar as partidas.",
                exception.getMessage()
        );

        verify(matchRepository, never()).findByGroupId(1L);
        verify(matchService, never()).createMatch(any(MatchRequestDTO.class));
    }

    @Test
    void shouldNotGenerateMatchesWhenGroupAlreadyHasMatches() {
        Group group = createGroupWithPlayers(1L, 1L, 2);

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByGroupId(1L))
                .thenReturn(List.of(new com.arenapointhub.api.model.Match()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupMatchService.generateRoundRobinMatchesForGroup(
                        1L,
                        "Mesa 1",
                        "10:00"
                )
        );

        assertEquals(
                "Já existem partidas geradas para este grupo.",
                exception.getMessage()
        );

        verify(matchService, never()).createMatch(any(MatchRequestDTO.class));
    }

    @Test
    void shouldNotGenerateMatchesWhenGroupDoesNotExist() {
        when(groupRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupMatchService.generateRoundRobinMatchesForGroup(
                        1L,
                        "Mesa 1",
                        "10:00"
                )
        );

        assertEquals(
                "Grupo não encontrado com ID: 1",
                exception.getMessage()
        );

        verify(matchRepository, never()).findByGroupId(1L);
        verify(matchService, never()).createMatch(any(MatchRequestDTO.class));
    }

    private Group createGroupWithPlayers(
            Long groupId,
            Long categoryId,
            int numberOfPlayers) {

        Group group = new Group();
        group.setId(groupId);
        group.setName("Grupo A");

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Sub 15");

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