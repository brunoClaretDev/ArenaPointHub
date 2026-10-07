package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.arenapointhub.api.dto.BracketResponseDTO;
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
class BracketServiceTest {

    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Sub 15";

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private BracketService bracketService;

    @BeforeEach
    void setUp() {
        bracketService = new BracketService(
                groupRepository,
                groupService,
                matchRepository,
                playerRepository,
                categoryRepository
        );
    }

    @Test
    void shouldReturnOrderedGroupWinners() {
        Category category = createCategory();

        Group groupA = createGroup(1L, "Grupo A", category);
        Group groupB = createGroup(2L, "Grupo B", category);

        Player playerA = createPlayer(1L, "Player A");
        Player playerB = createPlayer(2L, "Player B");

        mockGroupStandings(1L, playerA);
        mockGroupStandings(2L, playerB);
        mockPlayers(playerA, playerB);

        when(groupRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(new ArrayList<>(List.of(groupB, groupA)));

        List<Player> winners =
                bracketService.getOrderedGroupWinners(CATEGORY_ID);

        assertEquals(2, winners.size());
        assertEquals(1L, winners.get(0).getId());
        assertEquals(2L, winners.get(1).getId());

        verify(groupRepository).findByCategoryId(CATEGORY_ID);
    }

    @Test
    void shouldReturnFirstPlacePlayersBeforeSecondPlacePlayers() {
        Category category = createCategory();

        Group groupA = createGroup(1L, "Grupo A", category);
        Group groupB = createGroup(2L, "Grupo B", category);

        Player firstA = createPlayer(1L, "First A");
        Player secondA = createPlayer(2L, "Second A");
        Player firstB = createPlayer(3L, "First B");
        Player secondB = createPlayer(4L, "Second B");

        mockGroupStandings(1L, firstA, secondA);
        mockGroupStandings(2L, firstB, secondB);
        mockPlayers(firstA, secondA, firstB, secondB);

        when(groupRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(new ArrayList<>(List.of(groupB, groupA)));

        List<Player> players =
                bracketService.getOrderedQualifiedPlayers(CATEGORY_ID);

        assertEquals(4, players.size());
        assertEquals(1L, players.get(0).getId());
        assertEquals(3L, players.get(1).getId());
        assertEquals(2L, players.get(2).getId());
        assertEquals(4L, players.get(3).getId());
    }

    @Test
    void shouldCalculateRequiredByes() {
        assertEquals(
                2,
                bracketService.calculateRequiredByes(6, 8)
        );
    }

    @Test
    void shouldNotCalculateByesWhenQualifiedPlayersExceedBracketSize() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.calculateRequiredByes(9, 8)
        );

        assertEquals(
                "O número de classificados não pode ser maior que o tamanho da chave.",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotReturnGroupWinnersWhenCategoryHasNoGroups() {
        when(groupRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(new ArrayList<>());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.getOrderedGroupWinners(CATEGORY_ID)
        );

        assertEquals(
                "Não existem grupos para esta categoria.",
                exception.getMessage()
        );
    }

    @Test
    void shouldGenerateFirstRoundKnockoutBracket() {
        Category category = createCategory();
        List<Group> groups = createGroups(2, category);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");
        Player player4 = createPlayer(4L, "Player 4");

        mockCategoryAndGroups(category, groups);
        mockGroupStandings(1L, player1, player2);
        mockGroupStandings(2L, player3, player4);
        mockPlayers(player1, player2, player3, player4);
        mockMatchSave();

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(CATEGORY_ID, response.getCategoryId());
        assertEquals(CATEGORY_NAME, response.getCategoryName());
        assertEquals(4, response.getTargetBracketSize());
        assertEquals(3, response.getMatches().size());

        List<Match> matches = captureSavedMatches(3);

        assertEquals(MatchPhase.SEMI_FINAL, matches.get(0).getPhase());
        assertEquals(MatchPhase.SEMI_FINAL, matches.get(1).getPhase());
        assertEquals(MatchPhase.FINAL, matches.get(2).getPhase());
    }

    @Test
    void shouldDistributeByesAcrossDifferentSemifinals() {
        Category category = createCategory();
        List<Group> groups = createGroups(3, category);

        mockCategoryAndGroups(category, groups);
        mockQualifiedPlayers(3);
        mockMatchSave();

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(7, response.getMatches().size());

        List<Match> matches = captureSavedMatches(9);

        assertByeMatch(matches.get(0), 1L);
        assertByeMatch(matches.get(2), 3L);

        assertEquals(MatchPhase.SEMI_FINAL, matches.get(4).getPhase());
        assertEquals(MatchPhase.SEMI_FINAL, matches.get(5).getPhase());

        assertEquals(1L, matches.get(4).getPlayer1().getId());
        assertEquals(3L, matches.get(5).getPlayer1().getId());
    }

    @Test
    void shouldGenerateCorrectSeedingForEightGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 8);

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(16, response.getTargetBracketSize());
        assertEquals(15, response.getMatches().size());

        List<Match> matches = captureSavedMatches(15);

        assertMatch(matches.get(0), 1L, 14L);
        assertMatch(matches.get(1), 15L, 10L);
        assertMatch(matches.get(2), 11L, 4L);
        assertMatch(matches.get(3), 7L, 6L);
        assertMatch(matches.get(4), 5L, 12L);
        assertMatch(matches.get(5), 9L, 2L);
        assertMatch(matches.get(6), 13L, 8L);
        assertMatch(matches.get(7), 3L, 16L);
    }

    @Test
    void shouldGenerateCorrectSeedingForFourGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 4);

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(8, response.getTargetBracketSize());
        assertEquals(7, response.getMatches().size());

        List<Match> matches = captureSavedMatches(7);

        assertMatch(matches.get(0), 1L, 6L);
        assertMatch(matches.get(1), 7L, 4L);
        assertMatch(matches.get(2), 5L, 2L);
        assertMatch(matches.get(3), 3L, 8L);
    }

    @Test
    void shouldDistributeSixByesForFiveGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 5);

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(16, response.getTargetBracketSize());
        assertEquals(15, response.getMatches().size());

        List<Match> matches = captureSavedMatches(21);
        List<Match> firstRoundMatches = matches.subList(0, 8);

        long byeCount = firstRoundMatches.stream()
                .filter(this::isByeMatch)
                .count();

        assertEquals(6, byeCount);

        assertBye(firstRoundMatches, 1L);
        assertBye(firstRoundMatches, 3L);
        assertBye(firstRoundMatches, 5L);
        assertBye(firstRoundMatches, 7L);
        assertBye(firstRoundMatches, 9L);
        assertBye(firstRoundMatches, 4L);

        List<Match> scheduledMatches = firstRoundMatches.stream()
                .filter(match ->
                        match.getStatus() == MatchStatus.SCHEDULED)
                .toList();

        assertEquals(2, scheduledMatches.size());

        assertContainsPlayers(scheduledMatches, 6L, 8L);
        assertContainsPlayers(scheduledMatches, 2L, 10L);
    }

    @Test
    void shouldDistributeFourByesForSixGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 6);

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(16, response.getTargetBracketSize());
        assertEquals(15, response.getMatches().size());

        List<Match> matches = captureSavedMatches(19);
        List<Match> firstRoundMatches = matches.subList(0, 8);

        long byeCount = firstRoundMatches.stream()
                .filter(this::isByeMatch)
                .count();

        assertEquals(4, byeCount);

        assertBye(firstRoundMatches, 1L);
        assertBye(firstRoundMatches, 3L);
        assertBye(firstRoundMatches, 5L);
        assertBye(firstRoundMatches, 7L);

        List<Match> scheduledMatches = firstRoundMatches.stream()
                .filter(match ->
                        match.getStatus() == MatchStatus.SCHEDULED)
                .toList();

        assertEquals(4, scheduledMatches.size());

        assertContainsPlayers(scheduledMatches, 11L, 6L);
        assertContainsPlayers(scheduledMatches, 8L, 10L);
        assertContainsPlayers(scheduledMatches, 9L, 2L);
        assertContainsPlayers(scheduledMatches, 4L, 12L);
    }
    
    @Test
    void shouldDistributeTwoByesForSevenGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 7);

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(16, response.getTargetBracketSize());
        assertEquals(15, response.getMatches().size());

        List<Match> matches = captureSavedMatches(17);

        List<Match> firstRoundMatches =
                matches.subList(0, 8);

        long byeCount = firstRoundMatches.stream()
                .filter(this::isByeMatch)
                .count();

        assertEquals(2, byeCount);

        assertBye(firstRoundMatches, 1L);
        assertBye(firstRoundMatches, 3L);

        List<Match> scheduledMatches =
                firstRoundMatches.stream()
                        .filter(match ->
                                match.getStatus()
                                        == MatchStatus.SCHEDULED)
                        .toList();

        assertEquals(6, scheduledMatches.size());

        for (long playerId = 1L; playerId <= 14L; playerId++) {

            boolean playerFound = false;

            for (Match match : firstRoundMatches) {
                if (containsPlayer(match, playerId)) {
                    playerFound = true;
                    break;
                }
            }

            assertTrue(
                    playerFound,
                    "Jogador " + playerId
                            + " não foi encontrado na primeira rodada."
            );
        }
    }
    
    @Test
    void shouldGenerateCorrectSeedingForSevenGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 7);

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(16, response.getTargetBracketSize());
        assertEquals(15, response.getMatches().size());

        List<Match> matches = captureSavedMatches(17);

        List<Match> firstRoundMatches =
                matches.subList(0, 8);

        assertBye(firstRoundMatches, 1L);
        assertBye(firstRoundMatches, 3L);

        assertContainsPlayers(firstRoundMatches, 2L, 4L);
        assertContainsPlayers(firstRoundMatches, 11L, 6L);
        assertContainsPlayers(firstRoundMatches, 7L, 8L);
        assertContainsPlayers(firstRoundMatches, 5L, 10L);
        assertContainsPlayers(firstRoundMatches, 9L, 12L);
        assertContainsPlayers(firstRoundMatches, 13L, 14L);
    }
    
    @Test
    void shouldGenerateCorrectSeedingForTwoGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 2);

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(CATEGORY_ID);

        assertEquals(4, response.getTargetBracketSize());
        assertEquals(3, response.getMatches().size());

        List<Match> matches = captureSavedMatches(3);

        assertMatch(matches.get(0), 1L, 4L);
        assertMatch(matches.get(1), 2L, 3L);
    }

    private void prepareBracketScenario(
            Category category,
            int groupCount) {

        List<Group> groups =
                createGroups(groupCount, category);

        mockCategoryAndGroups(category, groups);
        mockQualifiedPlayers(groupCount);
        mockMatchSave();
    }

    private void mockQualifiedPlayers(int groupCount) {
        for (int groupId = 1; groupId <= groupCount; groupId++) {
            Player first = createPlayer(
                    groupId * 2L - 1,
                    "1." + groupId
            );

            Player second = createPlayer(
                    groupId * 2L,
                    "2." + groupId
            );

            mockGroupStandings(groupId, first, second);
            mockPlayers(first, second);
        }
    }

    private void mockGroupStandings(
            long groupId,
            Player... players) {

        List<GroupStandingDTO> standings =
                new ArrayList<>();

        for (Player player : players) {
            standings.add(createStanding(player));
        }

        when(groupService.calculateGroupStandings(groupId))
                .thenReturn(standings);
    }

    private void mockPlayers(Player... players) {
        for (Player player : players) {
            when(playerRepository.findById(player.getId()))
                    .thenReturn(Optional.of(player));
        }
    }

    private void mockCategoryAndGroups(
            Category category,
            List<Group> groups) {

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(groupRepository.findByCategoryId(category.getId()))
                .thenReturn(new ArrayList<>(groups));
    }

    private void mockMatchSave() {
        long[] nextId = {100L};

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> {
                    Match match = invocation.getArgument(0);
                    match.setId(nextId[0]++);
                    return match;
                });
    }

    private List<Match> captureSavedMatches(int expectedCount) {
        ArgumentCaptor<Match> captor =
                ArgumentCaptor.forClass(Match.class);

        verify(matchRepository, times(expectedCount))
                .save(captor.capture());

        return captor.getAllValues();
    }

    private void assertMatch(
            Match match,
            long player1Id,
            long player2Id) {

        assertEquals(player1Id, match.getPlayer1().getId());
        assertEquals(player2Id, match.getPlayer2().getId());
    }

    private void assertBye(
            List<Match> matches,
            long playerId) {

        assertTrue(
                matches.stream()
                        .anyMatch(match ->
                                isByeMatch(match)
                                && getByePlayerId(match) == playerId)
        );
    }

    private void assertByeMatch(
            Match match,
            long playerId) {

        assertEquals(MatchStatus.FINISHED, match.getStatus());
        assertEquals(playerId, getByePlayerId(match));
    }

    private boolean isByeMatch(Match match) {
        return match.getStatus() == MatchStatus.FINISHED
                && ((match.getPlayer1() != null
                        && match.getPlayer2() == null)
                    || (match.getPlayer1() == null
                        && match.getPlayer2() != null));
    }
    
    private boolean containsPlayer(
            Match match,
            long playerId) {

        return (match.getPlayer1() != null
                && match.getPlayer1().getId() == playerId)
                || (match.getPlayer2() != null
                && match.getPlayer2().getId() == playerId);
    }

    private long getByePlayerId(Match match) {
        if (match.getPlayer1() != null) {
            assertNull(match.getPlayer2());
            return match.getPlayer1().getId();
        }

        assertNull(match.getPlayer1());
        return match.getPlayer2().getId();
    }

    private void assertContainsPlayers(
            List<Match> matches,
            long player1Id,
            long player2Id) {

        assertTrue(
                matches.stream()
                        .anyMatch(match ->
                                match.getPlayer1() != null
                                && match.getPlayer2() != null
                                && samePlayers(
                                        match,
                                        player1Id,
                                        player2Id
                                ))
        );
    }

    private boolean samePlayers(
            Match match,
            long player1Id,
            long player2Id) {

        long actualPlayer1 =
                match.getPlayer1().getId();

        long actualPlayer2 =
                match.getPlayer2().getId();

        return (actualPlayer1 == player1Id
                && actualPlayer2 == player2Id)
                || (actualPlayer1 == player2Id
                && actualPlayer2 == player1Id);
    }

    private List<Group> createGroups(
            int quantity,
            Category category) {

        List<Group> groups = new ArrayList<>();

        for (int i = 1; i <= quantity; i++) {
            groups.add(
                    createGroup(
                            (long) i,
                            "Grupo " + (char) ('A' + i - 1),
                            category
                    )
            );
        }

        return groups;
    }

    private Category createCategory() {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName(CATEGORY_NAME);

        return category;
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

    private Player createPlayer(
            Long id,
            String name) {

        Player player = new Player();

        player.setId(id);
        player.setName(name);
        player.setEmail(
                name.toLowerCase()
                        .replace(" ", "")
                        + "@email.com"
        );
        player.setBirthDate(
                LocalDate.of(2012, 5, 10)
        );
        player.setClubAcademy("ArenaPoint");

        return player;
    }

    private GroupStandingDTO createStanding(
            Player player) {

        GroupStandingDTO standing =
                new GroupStandingDTO();

        standing.setPlayerId(player.getId());
        standing.setPlayerName(player.getName());
        standing.setClubAcademy(player.getClubAcademy());

        return standing;
    }
}