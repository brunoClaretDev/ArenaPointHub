package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.GroupGenerateRequestDTO;
import com.arenapointhub.api.dto.GroupRequestDTO;
import com.arenapointhub.api.dto.GroupResponseDTO;
import com.arenapointhub.api.dto.GroupStandingDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;

class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MatchRepository matchRepository;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        groupService = new GroupService(
                groupRepository,
                categoryRepository,
                playerRepository,
                matchRepository
        );
    }

    @Test
    void shouldCreateGroupWithPlayers() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        GroupRequestDTO dto = createGroupRequest(
                1L,
                "Grupo A",
                List.of(1L, 2L)
        );

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(player1, player2));

        when(groupRepository.existsByCategoryIdAndPlayersId(1L, 1L))
                .thenReturn(false);

        when(groupRepository.existsByCategoryIdAndPlayersId(1L, 2L))
                .thenReturn(false);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> {
                    Group group = invocation.getArgument(0);
                    group.setId(10L);
                    return group;
                });

        GroupResponseDTO response = groupService.createGroup(dto);

        assertEquals(10L, response.getId());
        assertEquals("Grupo A", response.getName());
        assertEquals(1L, response.getCategoryId());
        assertEquals(2, response.getPlayers().size());

        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void shouldNotCreateGroupWhenCategoryDoesNotExist() {
        GroupRequestDTO dto = createGroupRequest(
                1L,
                "Grupo A",
                List.of()
        );

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.createGroup(dto)
        );

        assertEquals(
                "Categoria não encontrada com ID: 1",
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldNotCreateGroupWhenPlayerDoesNotExist() {
        Category category = createCategory(1L);

        GroupRequestDTO dto = createGroupRequest(
                1L,
                "Grupo A",
                List.of(1L, 2L)
        );

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(createPlayer(1L, "Player 1")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.createGroup(dto)
        );

        assertEquals(
                "Um ou mais jogadores informados não foram encontrados.",
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldNotCreateGroupWhenPlayerAlreadyBelongsToAnotherGroup() {
        Category category = createCategory(1L);

        Player player = createPlayer(1L, "Player 1");

        GroupRequestDTO dto = createGroupRequest(
                1L,
                "Grupo A",
                List.of(1L)
        );

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(player));

        when(groupRepository.existsByCategoryIdAndPlayersId(1L, 1L))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.createGroup(dto)
        );

        assertEquals(
                "O jogador com ID 1 já pertence a um grupo desta categoria.",
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldReturnGroupsByCategory() {
        Category category = createCategory(1L);

        Group group1 = createGroup(1L, "Grupo A", category);
        Group group2 = createGroup(2L, "Grupo B", category);

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(List.of(group1, group2));

        List<GroupResponseDTO> response =
                groupService.getGroupsByCategory(1L);

        assertEquals(2, response.size());
        assertEquals("Grupo A", response.get(0).getName());
        assertEquals("Grupo B", response.get(1).getName());

        verify(groupRepository).findByCategoryId(1L);
    }

    @Test
    void shouldReturnGroupById() {
        Category category = createCategory(1L);
        Group group = createGroup(10L, "Grupo A", category);

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        GroupResponseDTO response =
                groupService.getGroupById(10L);

        assertEquals(10L, response.getId());
        assertEquals("Grupo A", response.getName());
        assertEquals(1L, response.getCategoryId());

        verify(groupRepository).findById(10L);
    }

    @Test
    void shouldUpdateGroup() {
        Category oldCategory = createCategory(1L);
        Category newCategory = createCategory(2L);

        Group group = createGroup(
                10L,
                "Grupo A",
                oldCategory
        );

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        group.setPlayers(new ArrayList<>(List.of(player1)));

        GroupRequestDTO dto = createGroupRequest(
                2L,
                "Grupo Atualizado",
                List.of(1L, 2L)
        );

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(newCategory));

        when(playerRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(player1, player2));

        when(groupRepository.existsByCategoryIdAndPlayersId(2L, 1L))
                .thenReturn(false);

        when(groupRepository.existsByCategoryIdAndPlayersId(2L, 2L))
                .thenReturn(false);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GroupResponseDTO response =
                groupService.updateGroup(10L, dto);

        assertEquals("Grupo Atualizado", response.getName());
        assertEquals(2L, response.getCategoryId());
        assertEquals(2, response.getPlayers().size());

        verify(groupRepository).save(group);
    }

    @Test
    void shouldGenerateAndDistributeGroups() {
        Category category = createCategory(1L);

        List<Player> players = new ArrayList<>();

        for (long i = 1; i <= 6; i++) {
            players.add(createPlayer(i, "Player " + i));
        }

        category.setPlayers(players);

        GroupGenerateRequestDTO dto = new GroupGenerateRequestDTO();
        dto.setCategoryId(1L);
        dto.setTargetGroupSize(3);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(List.of());

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> {
                    Group group = invocation.getArgument(0);
                    group.setId((long) (group.getPlayers().size()));
                    return group;
                });

        List<GroupResponseDTO> response =
                groupService.generateAndDistributeGroups(dto);

        assertEquals(2, response.size());

        ArgumentCaptor<Group> captor =
                ArgumentCaptor.forClass(Group.class);

        verify(groupRepository, times(2))
                .save(captor.capture());

        List<Group> savedGroups = captor.getAllValues();

        assertEquals("Grupo A", savedGroups.get(0).getName());
        assertEquals("Grupo B", savedGroups.get(1).getName());

        assertEquals(3, savedGroups.get(0).getPlayers().size());
        assertEquals(3, savedGroups.get(1).getPlayers().size());
    }

    @Test
    void shouldNotGenerateGroupsWhenCategoryAlreadyHasGroups() {
        Category category = createCategory(1L);

        Group existingGroup = createGroup(
                10L,
                "Grupo A",
                category
        );

        GroupGenerateRequestDTO dto = new GroupGenerateRequestDTO();
        dto.setCategoryId(1L);
        dto.setTargetGroupSize(3);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(List.of(existingGroup));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.generateAndDistributeGroups(dto)
        );

        assertEquals(
                "Já existem grupos gerados para esta categoria.",
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldCalculateGroupStandings() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Group group = createGroup(
                10L,
                "Grupo A",
                category
        );

        group.setPlayers(new ArrayList<>(List.of(player1, player2)));

        Match match = createMatch(
                player1,
                player2,
                MatchStatus.FINISHED,
                3,
                1
        );

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByGroupId(10L))
                .thenReturn(List.of(match));

        List<GroupStandingDTO> standings =
                groupService.calculateGroupStandings(10L);

        assertEquals(2, standings.size());

        assertEquals(1L, standings.get(0).getPlayerId());
        assertEquals(2, standings.get(0).getPoints());
        assertEquals(3, standings.get(0).getSetsWon());
        assertEquals(1, standings.get(0).getSetsLost());
        assertEquals(2, standings.get(0).getSetDifference());
        assertEquals(1, standings.get(0).getMatchesWon());

        assertEquals(2L, standings.get(1).getPlayerId());
        assertEquals(1, standings.get(1).getPoints());
        assertEquals(1, standings.get(1).getSetsWon());
        assertEquals(3, standings.get(1).getSetsLost());
        assertEquals(-2, standings.get(1).getSetDifference());
        assertEquals(1, standings.get(1).getMatchesLost());
    }

    @Test
    void shouldRemovePlayerFromGroupAndDeleteGroupMatches() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Group group = createGroup(
                10L,
                "Grupo A",
                category
        );

        group.setPlayers(
                new ArrayList<>(List.of(player1, player2))
        );

        Match match = createMatch(
                player1,
                player2,
                MatchStatus.SCHEDULED,
                0,
                0
        );

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByGroupId(10L))
                .thenReturn(List.of(match));

        groupService.removePlayerFromGroup(10L, 1L);

        assertEquals(1, group.getPlayers().size());
        assertFalse(
                group.getPlayers().stream()
                        .anyMatch(player -> player.getId().equals(1L))
        );

        verify(matchRepository).delete(match);
        verify(groupRepository).save(group);
    }

    private Category createCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Sub 15");
        return category;
    }

    private Player createPlayer(Long id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setEmail(
                name.toLowerCase().replace(" ", "") + "@email.com"
        );
        player.setBirthDate(LocalDate.of(2012, 5, 10));
        player.setClubAcademy("ArenaPoint");
        return player;
    }

    private Group createGroup(
            Long id,
            String name,
            Category category) {

        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setCategory(category);
        group.setPlayers(new ArrayList<>());

        return group;
    }

    private GroupRequestDTO createGroupRequest(
            Long categoryId,
            String name,
            List<Long> playerIds) {

        GroupRequestDTO dto = new GroupRequestDTO();
        dto.setCategoryId(categoryId);
        dto.setName(name);
        dto.setPlayerIds(playerIds);

        return dto;
    }

    private Match createMatch(
            Player player1,
            Player player2,
            MatchStatus status,
            int scorePlayer1,
            int scorePlayer2) {

        Match match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setStatus(status);
        match.setPhase(MatchPhase.GROUP);
        match.setScorePlayer1(scorePlayer1);
        match.setScorePlayer2(scorePlayer2);

        return match;
    }
}