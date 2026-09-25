package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.StandingDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;

class StandingServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MatchRepository matchRepository;

    private StandingService standingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        standingService = new StandingService(
                groupRepository,
                matchRepository
        );
    }

    @Test
    void shouldCalculateGroupStandings() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Group group = createGroup(
                10L,
                category,
                player1,
                player2
        );

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchStatus.FINISHED,
                3,
                1
        );

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByCategoryId(1L))
                .thenReturn(List.of(match));

        List<StandingDTO> standings =
                standingService.calculateGroupStandings(10L);

        assertEquals(2, standings.size());

        StandingDTO first = standings.get(0);
        StandingDTO second = standings.get(1);

        assertEquals(1L, first.getPlayerId());
        assertEquals(2, first.getPoints());
        assertEquals(1, first.getPlayed());
        assertEquals(1, first.getWins());
        assertEquals(0, first.getLosses());
        assertEquals(3, first.getSetsWon());
        assertEquals(1, first.getSetsLost());

        assertEquals(2L, second.getPlayerId());
        assertEquals(1, second.getPoints());
        assertEquals(1, second.getPlayed());
        assertEquals(0, second.getWins());
        assertEquals(1, second.getLosses());
        assertEquals(1, second.getSetsWon());
        assertEquals(3, second.getSetsLost());

        verify(groupRepository).findById(10L);
        verify(matchRepository).findByCategoryId(1L);
    }

    @Test
    void shouldIgnoreMatchesThatAreNotFinished() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Group group = createGroup(
                10L,
                category,
                player1,
                player2
        );

        Match scheduledMatch = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchStatus.SCHEDULED,
                3,
                1
        );

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByCategoryId(1L))
                .thenReturn(List.of(scheduledMatch));

        List<StandingDTO> standings =
                standingService.calculateGroupStandings(10L);

        assertEquals(2, standings.size());

        for (StandingDTO standing : standings) {
            assertEquals(0, standing.getPlayed());
            assertEquals(0, standing.getWins());
            assertEquals(0, standing.getLosses());
            assertEquals(0, standing.getPoints());
            assertEquals(0, standing.getSetsWon());
            assertEquals(0, standing.getSetsLost());
        }
    }

    @Test
    void shouldIgnoreMatchesWithPlayersOutsideGroup() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        Group group = createGroup(
                10L,
                category,
                player1,
                player2
        );

        Match matchOutsideGroup = createMatch(
                10L,
                category,
                player1,
                player3,
                MatchStatus.FINISHED,
                3,
                1
        );

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByCategoryId(1L))
                .thenReturn(List.of(matchOutsideGroup));

        List<StandingDTO> standings =
                standingService.calculateGroupStandings(10L);

        assertEquals(2, standings.size());

        for (StandingDTO standing : standings) {
            assertEquals(0, standing.getPlayed());
            assertEquals(0, standing.getPoints());
            assertEquals(0, standing.getWins());
            assertEquals(0, standing.getLosses());
        }
    }

    @Test
    void shouldReturnPlayersWithZeroStatisticsWhenTheyHaveNoFinishedMatches() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Group group = createGroup(
                10L,
                category,
                player1,
                player2
        );

        when(groupRepository.findById(10L))
                .thenReturn(Optional.of(group));

        when(matchRepository.findByCategoryId(1L))
                .thenReturn(List.of());

        List<StandingDTO> standings =
                standingService.calculateGroupStandings(10L);

        assertEquals(2, standings.size());

        for (StandingDTO standing : standings) {
            assertEquals(0, standing.getPlayed());
            assertEquals(0, standing.getWins());
            assertEquals(0, standing.getLosses());
            assertEquals(0, standing.getPoints());
            assertEquals(0, standing.getSetsWon());
            assertEquals(0, standing.getSetsLost());
        }
    }

    @Test
    void shouldNotCalculateStandingsWhenGroupDoesNotExist() {
        when(groupRepository.findById(10L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> standingService.calculateGroupStandings(10L)
        );

        assertEquals(
                "Grupo não encontrado com ID: 10",
                exception.getMessage()
        );
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
            Category category,
            Player... players) {

        Group group = new Group();
        group.setId(id);
        group.setName("Grupo A");
        group.setCategory(category);
        group.setPlayers(new ArrayList<>(List.of(players)));

        return group;
    }

    private Match createMatch(
            Long id,
            Category category,
            Player player1,
            Player player2,
            MatchStatus status,
            int scorePlayer1,
            int scorePlayer2) {

        Match match = new Match();
        match.setId(id);
        match.setCategory(category);
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setPhase(MatchPhase.GROUP);
        match.setStatus(status);
        match.setScorePlayer1(scorePlayer1);
        match.setScorePlayer2(scorePlayer2);

        return match;
    }
}