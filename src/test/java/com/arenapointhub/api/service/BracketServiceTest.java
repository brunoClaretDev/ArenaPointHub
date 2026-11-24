
package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    private static final Long ADMINISTRATOR_ID = 10L;
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

        lenient().when(matchRepository.existsByCategoryIdAndPhase(
                anyLong(), eq(MatchPhase.GROUP)))
                .thenReturn(true);

        lenient().when(matchRepository.existsByCategoryIdAndPhaseAndStatusNotIn(
                anyLong(), eq(MatchPhase.GROUP), anyList()))
                .thenReturn(false);
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

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(List.of(groupB, groupA)));

        List<Player> winners = bracketService.getOrderedGroupWinners(
                CATEGORY_ID, ADMINISTRATOR_ID);

        assertEquals(2, winners.size());
        assertEquals(1L, winners.get(0).getId());
        assertEquals(2L, winners.get(1).getId());

        verify(groupRepository)
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID);
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

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(List.of(groupB, groupA)));

        List<Player> players = bracketService.getOrderedQualifiedPlayers(
                CATEGORY_ID, ADMINISTRATOR_ID);

        assertEquals(4, players.size());
        assertEquals(1L, players.get(0).getId());
        assertEquals(3L, players.get(1).getId());
        assertEquals(2L, players.get(2).getId());
        assertEquals(4L, players.get(3).getId());
    }

    @Test
    void shouldCalculateRequiredByes() {
        assertEquals(2, bracketService.calculateRequiredByes(6, 8));
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
        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.getOrderedGroupWinners(
                        CATEGORY_ID, ADMINISTRATOR_ID)
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

        BracketResponseDTO response = bracketService.generateKnockoutBracket(
                CATEGORY_ID, ADMINISTRATOR_ID);

        assertEquals(CATEGORY_ID, response.getCategoryId());
        assertEquals(CATEGORY_NAME, response.getCategoryName());
        assertEquals(4, response.getTargetBracketSize());
        assertEquals(3, response.getMatches().size());

        List<Match> matches = captureSavedMatches(3);

        assertEquals(MatchPhase.SEMI_FINAL, matches.get(0).getPhase());
        assertEquals(MatchPhase.SEMI_FINAL, matches.get(1).getPhase());
        assertEquals(MatchPhase.FINAL, matches.get(2).getPhase());
    }

    @ParameterizedTest
    @MethodSource("spreadsheetModels")
    void shouldGenerateExactSeedingForEverySpreadsheetModel(
            int groupCount,
            String[] expectedSlots) {

        Category category = createCategory();
        prepareBracketScenario(category, groupCount);

        BracketResponseDTO response = bracketService.generateKnockoutBracket(
                CATEGORY_ID, ADMINISTRATOR_ID);

        int bracketSize = expectedSlots.length;
        int expectedByes = bracketSize - (groupCount * 2);
        int expectedMatchCount = bracketSize - 1;

        assertEquals(bracketSize, response.getTargetBracketSize());
        assertEquals(expectedMatchCount, response.getMatches().size());

        List<Match> savedMatches = captureSavedMatches(
                expectedMatchCount + expectedByes);
        List<Match> matches = savedMatches.subList(0, expectedMatchCount);
        List<Match> firstRound = matches.subList(0, bracketSize / 2);

        for (int i = 0; i < expectedSlots.length; i += 2) {
            String expectedPlayer1 = expectedSlots[i];
            String expectedPlayer2 = expectedSlots[i + 1];
            Match match = firstRound.get(i / 2);

            if (expectedPlayer1 != null && expectedPlayer2 != null) {
                assertMatch(
                        match,
                        expectedPlayerId(expectedPlayer1),
                        expectedPlayerId(expectedPlayer2));
            } else {
                assertTrue(
                        expectedPlayer1 != null || expectedPlayer2 != null,
                        "O modelo não pode criar confronto sem participantes.");

                long expectedByePlayerId = expectedPlayerId(
                        expectedPlayer1 != null ? expectedPlayer1 : expectedPlayer2);
                assertByeMatch(match, expectedByePlayerId);

                int targetIndex = (i / 2) / 2;
                Match nextRoundMatch = matches.get(bracketSize / 2 + targetIndex);

                if ((i / 2) % 2 == 0) {
                    assertEquals(expectedByePlayerId, nextRoundMatch.getPlayer1().getId());
                } else {
                    assertEquals(expectedByePlayerId, nextRoundMatch.getPlayer2().getId());
                }
            }
        }

        assertEquals(
                expectedByes,
                firstRound.stream().filter(this::isByeMatch).count());
    }

    private static java.util.stream.Stream<Arguments> spreadsheetModels() {
        return java.util.stream.Stream.of(
                Arguments.of(2, new String[] { "1.1", "2.2", "1.2", "2.1" }),
                Arguments.of(3, new String[] { "1.1", null, "2.3", "2.2", "1.3", "2.1", "1.2", null }),
                Arguments.of(4, new String[] { "1.1", "2.3", "1.4", "2.2", "1.3", "2.1", "1.2", "2.4" }),
                Arguments.of(5, new String[] { "1.1", null, "2.3", "2.5", null, "2.2", "1.4", null, "1.3", null, "1.5", null, "2.1", "2.4", "1.2", null }),
                Arguments.of(6, new String[] { "1.1", null, "2.3", "2.5", "1.6", "2.2", "1.4", null, "1.3", null, "1.5", "2.1", "2.6", "2.4", "1.2", null }),
                Arguments.of(7, new String[] { "1.1", null, "2.7", "2.5", "1.6", "2.2", "1.4", "2.3", "1.3", "2.6", "1.5", "2.1", "1.7", "2.4", "1.2", null }),
                Arguments.of(8, new String[] { "1.1", "2.7", "1.8", "2.5", "1.6", "2.2", "1.4", "2.3", "1.3", "2.6", "1.5", "2.1", "1.7", "2.4", "1.2", "2.8" }),
                Arguments.of(9, new String[] { "1.1", null, "2.2", null, "2.3", null, "1.8", null, "1.6", null, "2.9", "2.7", "2.5", null, "1.4", null, "1.3", null, null, "2.4", "2.8", "2.6", "1.5", null, "1.7", null, "1.9", null, null, "2.1", "1.2", null }),
                Arguments.of(10, new String[] { "1.1", null, "2.2", null, "1.10", null, "1.8", null, "1.6", null, "2.9", "2.5", "2.7", "2.3", "1.4", null, "1.3", null, "2.10", "2.4", "2.8", "2.6", "1.5", null, "1.7", null, "1.9", null, null, "2.1", "1.2", null }),
                Arguments.of(11, new String[] { "1.1", null, "2.2", "2.9", "1.10", null, "1.8", null, "1.6", null, "2.11", "2.5", "2.7", "2.3", "1.4", null, "1.3", null, "2.10", "2.4", "1.11", "2.6", "1.5", null, "1.7", null, "1.9", null, "2.8", "2.1", "1.2", null }),
                Arguments.of(12, new String[] { "1.1", null, "2.2", "2.9", "1.10", "2.11", "1.8", null, "1.6", null, "1.12", "2.5", "2.7", "2.3", "1.4", null, "1.3", null, "2.10", "2.4", "1.11", "2.6", "1.5", null, "1.7", null, "1.9", "2.8", "2.12", "2.1", "1.2", null }),
                Arguments.of(13, new String[] { "1.1", null, "2.2", "2.13", "1.10", "2.11", "1.8", "2.9", "1.6", null, "1.12", "2.5", "2.7", "2.3", "1.4", null, "1.3", null, "1.13", "2.4", "1.11", "2.6", "1.5", null, "1.7", "2.10", "1.9", "2.8", "2.12", "2.1", "1.2", null }),
                Arguments.of(14, new String[] { "1.1", null, "2.2", "2.13", "1.10", "2.11", "1.8", "2.9", "1.6", "2.7", "1.12", "2.5", "1.14", "2.3", "1.4", null, "1.3", null, "1.13", "2.4", "1.11", "2.6", "1.5", "2.12", "1.7", "2.10", "1.9", "2.8", "2.14", "2.1", "1.2", null }),
                Arguments.of(15, new String[] { "1.1", null, "2.15", "2.13", "1.10", "2.11", "1.8", "2.9", "1.6", "2.7", "1.12", "2.5", "1.14", "2.3", "1.4", "2.2", "1.3", "2.14", "1.13", "2.4", "1.11", "2.6", "1.5", "2.12", "1.7", "2.10", "1.9", "2.8", "1.15", "2.1", "1.2", null }),
                Arguments.of(16, new String[] { "1.1", "2.15", "1.16", "2.13", "1.10", "2.11", "1.8", "2.9", "1.6", "2.7", "1.12", "2.5", "1.14", "2.3", "1.4", "2.2", "1.3", "2.14", "1.13", "2.4", "1.11", "2.6", "1.5", "2.12", "1.7", "2.10", "1.9", "2.8", "1.15", "2.1", "1.2", "2.16" })
        );
    }

    private long expectedPlayerId(String seed) {
        int groupNumber = Integer.parseInt(seed.substring(2));
        return seed.startsWith("1.")
                ? (groupNumber * 2L) - 1L
                : groupNumber * 2L;
    }

    @Test
    void shouldBlockRegenerationWhenKnockoutMatchIsInProgress() {
        Category category = createCategory();

        List<MatchStatus> blockedStatuses = List.of(
                MatchStatus.IN_PROGRESS,
                MatchStatus.FINISHED,
                MatchStatus.WO
        );

        for (MatchStatus status : blockedStatuses) {
            Match existingMatch = new Match();
            existingMatch.setCategory(category);
            existingMatch.setPhase(MatchPhase.SEMI_FINAL);
            existingMatch.setStatus(status);

            when(categoryRepository
                    .findByIdAndTournament_Administrator_Id(
                            CATEGORY_ID, ADMINISTRATOR_ID))
                    .thenReturn(Optional.of(category));

            when(matchRepository.findByCategoryId(CATEGORY_ID))
                    .thenReturn(List.of(existingMatch));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> bracketService.generateKnockoutBracket(
                            CATEGORY_ID, ADMINISTRATOR_ID));

            assertEquals(
                    "Não é possível regenerar o mata-mata porque já existem partidas iniciadas ou encerradas.",
                    exception.getMessage()
            );

            verify(matchRepository, org.mockito.Mockito.never())
                    .deleteAll(any());
        }
    }

    @Test
    void shouldDeleteScheduledKnockoutMatchesBeforeRegeneration() {
        Category category = createCategory();
        List<Group> groups = createGroups(2, category);

        Match existingMatch = new Match();
        existingMatch.setCategory(category);
        existingMatch.setPhase(MatchPhase.SEMI_FINAL);
        existingMatch.setStatus(MatchStatus.SCHEDULED);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(matchRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(List.of(existingMatch));

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(groups));

        mockQualifiedPlayers(2);
        mockMatchSave();

        bracketService.generateKnockoutBracket(CATEGORY_ID, ADMINISTRATOR_ID);

        org.mockito.InOrder inOrder =
                org.mockito.Mockito.inOrder(matchRepository);

        inOrder.verify(matchRepository).deleteAll(List.of(existingMatch));
        inOrder.verify(matchRepository).flush();
        inOrder.verify(matchRepository, org.mockito.Mockito.atLeastOnce())
                .save(any(Match.class));
    }

    @Test
    void shouldPreserveGroupMatchesWhenRegeneratingKnockout() {
        Category category = createCategory();
        List<Group> groups = createGroups(2, category);

        Match groupMatch = new Match();
        groupMatch.setCategory(category);
        groupMatch.setPhase(MatchPhase.GROUP);
        groupMatch.setStatus(MatchStatus.FINISHED);

        Match knockoutMatch = new Match();
        knockoutMatch.setCategory(category);
        knockoutMatch.setPhase(MatchPhase.SEMI_FINAL);
        knockoutMatch.setStatus(MatchStatus.SCHEDULED);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(matchRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(List.of(groupMatch, knockoutMatch));

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(groups));

        mockQualifiedPlayers(2);
        mockMatchSave();

        bracketService.generateKnockoutBracket(CATEGORY_ID, ADMINISTRATOR_ID);

        ArgumentCaptor<List<Match>> captor =
                ArgumentCaptor.captor();

        org.mockito.InOrder inOrder =
                org.mockito.Mockito.inOrder(matchRepository);

        inOrder.verify(matchRepository).deleteAll(captor.capture());
        inOrder.verify(matchRepository).flush();
        inOrder.verify(matchRepository, org.mockito.Mockito.atLeastOnce())
                .save(any(Match.class));

        assertTrue(captor.getValue().contains(knockoutMatch));
        assertFalse(captor.getValue().contains(groupMatch));
    }

    @Test
    void shouldGenerateFinalDirectlyForOneGroup() {
        Category category = createCategory();

        prepareBracketScenario(category, 1);

        BracketResponseDTO response = bracketService.generateKnockoutBracket(
                CATEGORY_ID, ADMINISTRATOR_ID);

        assertEquals(2, response.getTargetBracketSize());
        assertEquals(1, response.getMatches().size());

        List<Match> matches = captureSavedMatches(1);

        assertEquals(MatchPhase.FINAL, matches.get(0).getPhase());
        assertMatch(matches.get(0), 1L, 2L);
        assertEquals(MatchStatus.SCHEDULED, matches.get(0).getStatus());
    }

    @Test
    void shouldRejectKnockoutGenerationWhenCategoryDoesNotExist() {
        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.generateKnockoutBracket(
                        CATEGORY_ID, ADMINISTRATOR_ID));

        assertEquals(
                "Categoria não encontrada com ID: 1",
                exception.getMessage());

        verify(matchRepository, org.mockito.Mockito.never())
                .save(any(Match.class));

        verify(matchRepository, org.mockito.Mockito.never())
                .findByCategoryId(CATEGORY_ID);
    }

    @Test
    void shouldRejectKnockoutGenerationWhenCategoryHasNoGroups() {
        Category category = createCategory();

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.generateKnockoutBracket(
                        CATEGORY_ID, ADMINISTRATOR_ID));

        assertEquals(
                "Não existem grupos para esta categoria.",
                exception.getMessage());

        verify(matchRepository, org.mockito.Mockito.never())
                .save(any(Match.class));
    }

    @Test
    void shouldGenerateBracketForNineGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 9);

        BracketResponseDTO response = bracketService.generateKnockoutBracket(
                CATEGORY_ID, ADMINISTRATOR_ID);

        assertEquals(32, response.getTargetBracketSize());
        assertEquals(31, response.getMatches().size());

        List<Match> matches = captureSavedMatches(45);
        List<Match> firstRound = matches.subList(0, 16);

        assertEquals(
                14,
                firstRound.stream().filter(this::isByeMatch).count());
    }
    
    @Test
    void shouldGenerateBracketForSixteenGroups() {
        Category category = createCategory();

        prepareBracketScenario(category, 16);

        BracketResponseDTO response = bracketService.generateKnockoutBracket(
                CATEGORY_ID, ADMINISTRATOR_ID);

        assertEquals(32, response.getTargetBracketSize());
        assertEquals(31, response.getMatches().size());

        captureSavedMatches(31);
    }

    @Test
    void shouldRejectBracketForSeventeenGroups() {
        Category category = createCategory();

        List<Group> groups = createGroups(17, category);

        mockCategoryAndGroups(category, groups);
        mockQualifiedPlayers(17);

        assertThrows(
                BusinessException.class,
                () -> bracketService.generateKnockoutBracket(
                        CATEGORY_ID, ADMINISTRATOR_ID));

        verify(matchRepository, org.mockito.Mockito.never())
                .save(any(Match.class));
    }

    @Test
    void shouldRejectGroupWithIncompleteClassification() {
        Category category = createCategory();
        List<Group> groups = createGroups(2, category);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(matchRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(List.of());

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(groups));

        Player firstPlace = createPlayer(1L, "Player 1");
        Player secondPlace = createPlayer(2L, "Player 2");
        Player thirdPlace = createPlayer(3L, "Player 3");

        mockGroupStandings(1L, firstPlace, secondPlace);
        mockGroupStandings(2L, thirdPlace);
        mockPlayers(firstPlace, secondPlace, thirdPlace);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.generateKnockoutBracket(
                        CATEGORY_ID, ADMINISTRATOR_ID));

        assertEquals(
                "Cada grupo precisa ter dois jogadores classificados.",
                exception.getMessage());

        verify(matchRepository, org.mockito.Mockito.never())
                .save(any(Match.class));
    }

    @Test
    void shouldRejectQualifiedPlayerNotFoundInRepository() {
        Category category = createCategory();
        List<Group> groups = createGroups(2, category);

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(matchRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(List.of());

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(groups));

        Player firstPlace = createPlayer(1L, "Player 1");
        Player secondPlace = createPlayer(2L, "Player 2");
        Player thirdPlace = createPlayer(3L, "Player 3");
        Player fourthPlace = createPlayer(4L, "Player 4");

        mockGroupStandings(1L, firstPlace, secondPlace);
        mockGroupStandings(2L, thirdPlace, fourthPlace);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(firstPlace));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(secondPlace));
        when(playerRepository.findById(3L)).thenReturn(Optional.of(thirdPlace));
        when(playerRepository.findById(4L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.generateKnockoutBracket(
                        CATEGORY_ID, ADMINISTRATOR_ID));

        assertEquals(
                "Cada grupo precisa ter dois jogadores classificados.",
                exception.getMessage());

        verify(matchRepository, org.mockito.Mockito.never())
                .save(any(Match.class));
    }

    @Test
    void shouldRejectDuplicateQualifiedPlayers() {
        Category category = createCategory();
        List<Group> groups = createGroups(2, category);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(matchRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(List.of());

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(groups));

        GroupStandingDTO firstGroupWinner =
                org.mockito.Mockito.mock(GroupStandingDTO.class);
        GroupStandingDTO firstGroupSecond =
                org.mockito.Mockito.mock(GroupStandingDTO.class);
        GroupStandingDTO secondGroupWinner =
                org.mockito.Mockito.mock(GroupStandingDTO.class);
        GroupStandingDTO secondGroupSecond =
                org.mockito.Mockito.mock(GroupStandingDTO.class);

        when(firstGroupWinner.getPlayerId()).thenReturn(1L);
        when(firstGroupSecond.getPlayerId()).thenReturn(2L);
        when(secondGroupWinner.getPlayerId()).thenReturn(3L);
        when(secondGroupSecond.getPlayerId()).thenReturn(2L);

        when(groupService.calculateGroupStandings(
                groups.get(0).getId(), ADMINISTRATOR_ID))
                .thenReturn(List.of(firstGroupWinner, firstGroupSecond));

        when(groupService.calculateGroupStandings(
                groups.get(1).getId(), ADMINISTRATOR_ID))
                .thenReturn(List.of(secondGroupWinner, secondGroupSecond));

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(playerRepository.findById(3L)).thenReturn(Optional.of(player3));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.generateKnockoutBracket(
                        CATEGORY_ID, ADMINISTRATOR_ID));

        assertEquals(
                "Existem jogadores duplicados entre os classificados.",
                exception.getMessage());

        verify(matchRepository, org.mockito.Mockito.never())
                .save(any(Match.class));
    }

    private void prepareBracketScenario(
            Category category,
            int groupCount) {

        List<Group> groups = createGroups(groupCount, category);

        mockCategoryAndGroups(category, groups);
        mockQualifiedPlayers(groupCount);
        mockMatchSave();
    }

    private void mockQualifiedPlayers(int groupCount) {
        for (int groupId = 1; groupId <= groupCount; groupId++) {
            Player first = createPlayer(
                    groupId * 2L - 1,
                    "1." + groupId);

            Player second = createPlayer(
                    groupId * 2L,
                    "2." + groupId);

            mockGroupStandings(groupId, first, second);
            mockPlayers(first, second);
        }
    }

    private void mockGroupStandings(
            long groupId,
            Player... players) {

        List<GroupStandingDTO> standings = new ArrayList<>();

        for (Player player : players) {
            standings.add(createStanding(player));
        }

        when(groupService.calculateGroupStandings(
                groupId, ADMINISTRATOR_ID))
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

        when(categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        category.getId(), ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));

        when(groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        category.getId(), ADMINISTRATOR_ID))
                .thenReturn(new ArrayList<>(groups));

        when(matchRepository.findByCategoryId(category.getId()))
                .thenReturn(new ArrayList<>());
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

        assertTrue(matches.stream()
                .anyMatch(match ->
                        isByeMatch(match)
                        && getByePlayerId(match) == playerId));
    }

    private void assertByeMatch(
            Match match,
            long playerId) {

        assertEquals(MatchStatus.FINISHED, match.getStatus());
        assertEquals(playerId, getByePlayerId(match));
    }

    private boolean isByeMatch(Match match) {
        return match.getStatus() == MatchStatus.FINISHED
                && ((match.getPlayer1() != null && match.getPlayer2() == null)
                || (match.getPlayer1() == null && match.getPlayer2() != null));
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

        assertTrue(matches.stream()
                .anyMatch(match ->
                        match.getPlayer1() != null
                        && match.getPlayer2() != null
                        && samePlayers(match, player1Id, player2Id)));
    }

    private boolean samePlayers(
            Match match,
            long player1Id,
            long player2Id) {

        long actualPlayer1 = match.getPlayer1().getId();
        long actualPlayer2 = match.getPlayer2().getId();

        return (actualPlayer1 == player1Id && actualPlayer2 == player2Id)
                || (actualPlayer1 == player2Id && actualPlayer2 == player1Id);
    }

    private List<Group> createGroups(
            int quantity,
            Category category) {

        List<Group> groups = new ArrayList<>();

        for (int i = 1; i <= quantity; i++) {
            groups.add(createGroup(
                    (long) i,
                    "Grupo " + (char) ('A' + i - 1),
                    category));
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
        player.setEmail(name.toLowerCase().replace(" ", "") + "@email.com");
        player.setBirthDate(LocalDate.of(2012, 5, 10));
        player.setClubAcademy("ArenaPoint");

        return player;
    }

    private GroupStandingDTO createStanding(Player player) {
        GroupStandingDTO standing = new GroupStandingDTO();

        standing.setPlayerId(player.getId());
        standing.setPlayerName(player.getName());
        standing.setClubAcademy(player.getClubAcademy());

        return standing;
    }
}
