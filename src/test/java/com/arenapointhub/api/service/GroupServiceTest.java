
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private static final Long ADMINISTRATOR_ID = 10L;
    private static final Long OTHER_ADMINISTRATOR_ID = 20L;

    private static final Long CATEGORY_ID = 1L;
    private static final Long OTHER_CATEGORY_ID = 2L;
    private static final Long GROUP_ID = 10L;

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
        groupService = new GroupService(
                groupRepository,
                categoryRepository,
                playerRepository,
                matchRepository
        );
    }

    @Test
    void shouldCreateGroupWithPlayers() {
        Category category = createCategory(CATEGORY_ID);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        GroupRequestDTO dto = createGroupRequest(
                CATEGORY_ID,
                "Grupo A",
                List.of(1L, 2L)
        );

        mockOwnedCategory(category);

        when(playerRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(player1, player2));

        when(groupRepository.existsByCategoryIdAndPlayersId(
                CATEGORY_ID, 1L))
                .thenReturn(false);

        when(groupRepository.existsByCategoryIdAndPlayersId(
                CATEGORY_ID, 2L))
                .thenReturn(false);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> {
                    Group group = invocation.getArgument(0);
                    group.setId(GROUP_ID);
                    return group;
                });

        GroupResponseDTO response = groupService.createGroup(
                dto, ADMINISTRATOR_ID);

        assertEquals(GROUP_ID, response.getId());
        assertEquals("Grupo A", response.getName());
        assertEquals(CATEGORY_ID, response.getCategoryId());
        assertEquals(2, response.getPlayers().size());

        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void shouldNotCreateGroupWhenCategoryDoesNotBelongToAdministrator() {
        GroupRequestDTO dto = createGroupRequest(
                CATEGORY_ID,
                "Grupo A",
                List.of()
        );

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.createGroup(dto, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Categoria não encontrada com ID: " + CATEGORY_ID,
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldNotCreateGroupWhenPlayerDoesNotExist() {
        Category category = createCategory(CATEGORY_ID);

        GroupRequestDTO dto = createGroupRequest(
                CATEGORY_ID,
                "Grupo A",
                List.of(1L, 2L)
        );

        mockOwnedCategory(category);

        when(playerRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(createPlayer(1L, "Player 1")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.createGroup(dto, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Um ou mais jogadores informados não foram encontrados.",
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldNotCreateGroupWhenPlayerAlreadyBelongsToAnotherGroup() {
        Category category = createCategory(CATEGORY_ID);

        Player player = createPlayer(1L, "Player 1");

        GroupRequestDTO dto = createGroupRequest(
                CATEGORY_ID,
                "Grupo A",
                List.of(1L)
        );

        mockOwnedCategory(category);

        when(playerRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(player));

        when(groupRepository.existsByCategoryIdAndPlayersId(
                CATEGORY_ID, 1L))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.createGroup(dto, ADMINISTRATOR_ID)
        );

        assertEquals(
                "O jogador com ID 1 já pertence a um grupo desta categoria.",
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldReturnGroupsByCategory() {
        Category category = createCategory(CATEGORY_ID);

        Group groupA = createGroup(1L, "Grupo A", category);
        Group groupB = createGroup(2L, "Grupo B", category);

        mockOwnedCategory(category);

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(List.of(groupA, groupB));

        List<GroupResponseDTO> response = groupService.getGroupsByCategory(
                CATEGORY_ID, ADMINISTRATOR_ID);

        assertEquals(2, response.size());
        assertEquals("Grupo A", response.get(0).getName());
        assertEquals("Grupo B", response.get(1).getName());

        verify(groupRepository)
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldNotReturnGroupsWhenCategoryDoesNotBelongToAdministrator() {
        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.getGroupsByCategory(
                        CATEGORY_ID, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Categoria não encontrada com ID: " + CATEGORY_ID,
                exception.getMessage()
        );

        verify(groupRepository, never())
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        any(), any());
    }

    @Test
    void shouldReturnGroupById() {
        Category category = createCategory(CATEGORY_ID);
        Group group = createGroup(GROUP_ID, "Grupo A", category);

        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(group));

        GroupResponseDTO response = groupService.getGroupById(
                GROUP_ID, ADMINISTRATOR_ID);

        assertEquals(GROUP_ID, response.getId());
        assertEquals("Grupo A", response.getName());
        assertEquals(CATEGORY_ID, response.getCategoryId());

        verify(groupRepository)
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldNotReturnGroupWhenItDoesNotBelongToAdministrator() {
        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.getGroupById(
                        GROUP_ID, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Grupo não encontrado com ID: " + GROUP_ID,
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateGroup() {
        Category oldCategory = createCategory(CATEGORY_ID);
        Category newCategory = createCategory(OTHER_CATEGORY_ID);

        Group group = createGroup(GROUP_ID, "Grupo A", oldCategory);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        group.setPlayers(new ArrayList<>(List.of(player1)));

        GroupRequestDTO dto = createGroupRequest(
                OTHER_CATEGORY_ID,
                "Grupo Atualizado",
                List.of(1L, 2L)
        );

        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(group));

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        OTHER_CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(newCategory));

        when(playerRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(player1, player2));

        when(groupRepository.existsByCategoryIdAndPlayersId(
                OTHER_CATEGORY_ID, 1L))
                .thenReturn(false);

        when(groupRepository.existsByCategoryIdAndPlayersId(
                OTHER_CATEGORY_ID, 2L))
                .thenReturn(false);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GroupResponseDTO response = groupService.updateGroup(
                GROUP_ID, dto, ADMINISTRATOR_ID);

        assertEquals("Grupo Atualizado", response.getName());
        assertEquals(OTHER_CATEGORY_ID, response.getCategoryId());
        assertEquals(2, response.getPlayers().size());

        verify(groupRepository).save(group);
    }

    @Test
    void shouldNotUpdateGroupWhenGroupDoesNotBelongToAdministrator() {
        GroupRequestDTO dto = createGroupRequest(
                OTHER_CATEGORY_ID,
                "Grupo Atualizado",
                List.of()
        );

        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.updateGroup(
                        GROUP_ID, dto, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Grupo não encontrado com ID: " + GROUP_ID,
                exception.getMessage()
        );

        verify(categoryRepository, never())
                .findByIdAndTournament_Administrator_Id(
                        any(), any());

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldNotUpdateGroupWhenDestinationCategoryDoesNotBelongToAdministrator() {
        Category category = createCategory(CATEGORY_ID);
        Group group = createGroup(GROUP_ID, "Grupo A", category);

        GroupRequestDTO dto = createGroupRequest(
                OTHER_CATEGORY_ID,
                "Grupo Atualizado",
                List.of()
        );

        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(group));

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        OTHER_CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.updateGroup(
                        GROUP_ID, dto, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Categoria não encontrada com ID: " + OTHER_CATEGORY_ID,
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldGenerateAndDistributeGroups() {
        Category category = createCategory(CATEGORY_ID);

        List<Player> players = new ArrayList<>();

        for (long i = 1; i <= 6; i++) {
            players.add(createPlayer(i, "Player " + i));
        }

        category.setPlayers(players);

        GroupGenerateRequestDTO dto = new GroupGenerateRequestDTO();
        dto.setCategoryId(CATEGORY_ID);
        dto.setTargetGroupSize(3);

        mockOwnedCategory(category);

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(List.of());

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> {
                    Group group = invocation.getArgument(0);
                    group.setId((long) group.getPlayers().size());
                    return group;
                });

        List<GroupResponseDTO> response =
                groupService.generateAndDistributeGroups(
                        dto, ADMINISTRATOR_ID);

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
        Category category = createCategory(CATEGORY_ID);

        Group existingGroup = createGroup(
                GROUP_ID, "Grupo A", category);

        GroupGenerateRequestDTO dto = new GroupGenerateRequestDTO();
        dto.setCategoryId(CATEGORY_ID);
        dto.setTargetGroupSize(3);

        mockOwnedCategory(category);

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(List.of(existingGroup));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.generateAndDistributeGroups(
                        dto, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Já existem grupos gerados para esta categoria.",
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldNotGenerateGroupsWhenCategoryDoesNotBelongToAdministrator() {
        GroupGenerateRequestDTO dto = new GroupGenerateRequestDTO();
        dto.setCategoryId(CATEGORY_ID);
        dto.setTargetGroupSize(3);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.generateAndDistributeGroups(
                        dto, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Categoria não encontrada com ID: " + CATEGORY_ID,
                exception.getMessage()
        );

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldCalculateGroupStandings() {
        Category category = createCategory(CATEGORY_ID);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Group group = createGroup(GROUP_ID, "Grupo A", category);
        group.setPlayers(new ArrayList<>(List.of(player1, player2)));

        Match match = createMatch(
                player1,
                player2,
                MatchStatus.FINISHED,
                3,
                1
        );

        mockOwnedGroup(group);

        when(matchRepository.findByGroupId(GROUP_ID))
                .thenReturn(List.of(match));

        List<GroupStandingDTO> standings =
                groupService.calculateGroupStandings(
                        GROUP_ID, ADMINISTRATOR_ID);

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
    void shouldPrioritizeMostWinsWhenPlayersHaveSamePoints() {
        Category category = createCategory(CATEGORY_ID);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");
        Player player4 = createPlayer(4L, "Player 4");

        Group group = createGroup(GROUP_ID, "Grupo A", category);
        group.setPlayers(new ArrayList<>(
                List.of(player1, player2, player3, player4)
        ));

        List<Match> matches = List.of(
                createMatch(player1, player3, MatchStatus.FINISHED, 3, 0),
                createMatch(player1, player4, MatchStatus.FINISHED, 3, 0),
                createMatch(player2, player3, MatchStatus.FINISHED, 3, 0),
                createMatch(player2, player4, MatchStatus.FINISHED, 0, 3),
                createMatch(player2, player3, MatchStatus.FINISHED, 0, 3)
        );

        mockOwnedGroup(group);

        when(matchRepository.findByGroupId(GROUP_ID))
                .thenReturn(matches);

        List<GroupStandingDTO> standings =
                groupService.calculateGroupStandings(
                        GROUP_ID, ADMINISTRATOR_ID);

        GroupStandingDTO first = standings.get(0);
        GroupStandingDTO second = standings.get(1);

        assertEquals(1L, first.getPlayerId());
        assertEquals(4, first.getPoints());
        assertEquals(2, first.getMatchesWon());

        assertEquals(2L, second.getPlayerId());
        assertEquals(4, second.getPoints());
        assertEquals(1, second.getMatchesWon());
    }

    @Test
    void shouldPrioritizeSetDifferenceWhenPointsAndWinsAreEqual() {
        Category category = createCategory(CATEGORY_ID);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        Group group = createGroup(GROUP_ID, "Grupo A", category);
        group.setPlayers(new ArrayList<>(
                List.of(player1, player2, player3)
        ));

        List<Match> matches = List.of(
                createMatch(player1, player3, MatchStatus.FINISHED, 3, 0),
                createMatch(player2, player3, MatchStatus.FINISHED, 3, 2)
        );

        mockOwnedGroup(group);

        when(matchRepository.findByGroupId(GROUP_ID))
                .thenReturn(matches);

        List<GroupStandingDTO> standings =
                groupService.calculateGroupStandings(
                        GROUP_ID, ADMINISTRATOR_ID);

        GroupStandingDTO first = standings.get(0);
        GroupStandingDTO second = standings.get(1);

        assertEquals(1L, first.getPlayerId());
        assertEquals(2, first.getPoints());
        assertEquals(1, first.getMatchesWon());
        assertEquals(3, first.getSetDifference());

        assertEquals(2L, second.getPlayerId());
        assertEquals(2, second.getPoints());
        assertEquals(1, second.getMatchesWon());
        assertEquals(1, second.getSetDifference());
    }

    @Test
    void shouldPrioritizeSetsWonWhenPointsWinsAndSetDifferenceAreEqual() {
        Category category = createCategory(CATEGORY_ID);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");
        Player player4 = createPlayer(4L, "Player 4");

        Group group = createGroup(GROUP_ID, "Grupo A", category);
        group.setPlayers(new ArrayList<>(
                List.of(player1, player2, player3, player4)
        ));

        List<Match> matches = List.of(
                createMatch(player1, player3, MatchStatus.FINISHED, 3, 0),
                createMatch(player4, player1, MatchStatus.FINISHED, 3, 1),
                createMatch(player2, player4, MatchStatus.FINISHED, 4, 0),
                createMatch(player3, player2, MatchStatus.FINISHED, 5, 2)
        );

        mockOwnedGroup(group);

        when(matchRepository.findByGroupId(GROUP_ID))
                .thenReturn(matches);

        List<GroupStandingDTO> standings =
                groupService.calculateGroupStandings(
                        GROUP_ID, ADMINISTRATOR_ID);

        GroupStandingDTO first = standings.get(0);
        GroupStandingDTO second = standings.get(1);

        assertEquals(2L, first.getPlayerId());
        assertEquals(3, first.getPoints());
        assertEquals(1, first.getMatchesWon());
        assertEquals(1, first.getSetDifference());
        assertEquals(6, first.getSetsWon());

        assertEquals(1L, second.getPlayerId());
        assertEquals(3, second.getPoints());
        assertEquals(1, second.getMatchesWon());
        assertEquals(1, second.getSetDifference());
        assertEquals(4, second.getSetsWon());
    }

    @Test
    void shouldNotCalculateStandingsWhenGroupDoesNotBelongToAdministrator() {
        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.calculateGroupStandings(
                        GROUP_ID, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Grupo não encontrado com ID: " + GROUP_ID,
                exception.getMessage()
        );

        verify(matchRepository, never()).findByGroupId(GROUP_ID);
    }


	@Test
	void shouldRemovePlayerFromGroupAndDeleteGroupMatches() {
	    Category category = createCategory(CATEGORY_ID);
	
	    Player player1 = createPlayer(1L, "Player 1");
	    Player player2 = createPlayer(2L, "Player 2");
	
	    Group group = createGroup(
	            GROUP_ID,
	            "Grupo A",
	            category
	    );
	
	    group.setPlayers(new ArrayList<>(List.of(player1, player2)));
	
	    Match match = createMatch(
	            player1,
	            player2,
	            MatchStatus.SCHEDULED,
	            0,
	            0
	    );
	
	    mockOwnedGroup(group);
	
	    when(matchRepository.findByGroupId(GROUP_ID))
	            .thenReturn(List.of(match));
	
	    groupService.removePlayerFromGroup(
	            GROUP_ID,
	            1L,
	            ADMINISTRATOR_ID
	    );
	
	    assertEquals(1, group.getPlayers().size());
	    assertFalse(
	            group.getPlayers().stream()
	                    .anyMatch(player -> player.getId().equals(1L))
	    );
	
	    verify(matchRepository).delete(match);
	    verify(groupRepository).save(group);
	}


    @Test
    void shouldNotRemovePlayerWhenGroupDoesNotBelongToAdministrator() {
        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.removePlayerFromGroup(
                        GROUP_ID, 1L, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Grupo não encontrado com ID: " + GROUP_ID,
                exception.getMessage()
        );

        verify(matchRepository, never()).findByGroupId(GROUP_ID);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldNotRemovePlayerWhenGroupHasFinishedMatches() {
        Category category = createCategory(CATEGORY_ID);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Group group = createGroup(GROUP_ID, "Grupo A", category);
        group.setPlayers(new ArrayList<>(List.of(player1, player2)));

        Match finishedMatch = createMatch(
                player1,
                player2,
                MatchStatus.FINISHED,
                3,
                1
        );

        mockOwnedGroup(group);

        when(matchRepository.findByGroupId(GROUP_ID))
                .thenReturn(List.of(finishedMatch));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> groupService.removePlayerFromGroup(
                        GROUP_ID, 1L, ADMINISTRATOR_ID)
        );

        assertEquals(
                "Não é possível remover jogadores de um grupo que já possui partidas iniciadas ou realizadas.",
                exception.getMessage()
        );

        verify(matchRepository, never()).deleteAll(any());
        verify(groupRepository, never()).save(any(Group.class));
    }

    private void mockOwnedCategory(Category category) {
        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        category.getId(), ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));
    }

    private void mockOwnedGroup(Group group) {
        when(groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        group.getId(), ADMINISTRATOR_ID))
                .thenReturn(Optional.of(group));
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
                name.toLowerCase().replace(" ", "") + "@email.com");
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
