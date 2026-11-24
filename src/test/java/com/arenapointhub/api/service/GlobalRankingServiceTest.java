package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.PlayerGlobalRanking;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.TournamentScoringConfig;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.model.enums.ScoringSystemType;
import com.arenapointhub.api.repository.PlayerGlobalRankingRepository;
import com.arenapointhub.api.repository.TournamentScoringConfigRepository;

class GlobalRankingServiceTest {

    @Mock
    private PlayerGlobalRankingRepository rankingRepository;

    @Mock
    private TournamentScoringConfigRepository scoringConfigRepository;

    private GlobalRankingService globalRankingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        globalRankingService = new GlobalRankingService(
                rankingRepository,
                scoringConfigRepository
        );
    }

    @Test
    void shouldDistributeClassicPoints() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);
        config.setPointsRunnerUp(70);

        Map<Player, Integer> positions = Map.of(
                champion, 1,
                runnerUp, 2
        );

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.distributeTournamentPoints(
                tournament,
                category,
                positions
        );

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 70, 1);

        assertEquals(true, category.isRankingProcessed());
    }

    @Test
    void shouldDistributeByPositionPoints() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player player = createPlayer(1L, "Player");

        TournamentScoringConfig config = createByPositionConfig();
        config.setPoints1stPlace(150);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.distributeTournamentPoints(
                tournament,
                category,
                Map.of(player, 1)
        );

        verifyRankingSaved(player, 150, 1);
    }

    @Test
    void shouldCreateRankingWhenPlayerHasNoRankingForCurrentYear() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);
        Player player = createPlayer(1L, "Player");

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.distributeTournamentPoints(
                tournament,
                category,
                Map.of(player, 1)
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(player)
                                && ranking.getCategory().equals(category)
                                && ranking.getYear() == LocalDate.now().getYear()
                                && ranking.getTotalPoints() == 100
                                && ranking.getTournamentsPlayed() == 1
                )
        );
    }

    @Test
    void shouldUpdateExistingRanking() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);
        Player player = createPlayer(1L, "Player");

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);

        PlayerGlobalRanking ranking = new PlayerGlobalRanking();
        ranking.setPlayer(player);
        ranking.setCategory(category);
        ranking.setYear(LocalDate.now().getYear());
        ranking.setTotalPoints(50);
        ranking.setTournamentsPlayed(2);

        mockScoringConfig(config);

        when(rankingRepository.findByPlayerIdAndCategoryIdAndYear(
                1L,
                1L,
                LocalDate.now().getYear()
        )).thenReturn(Optional.of(ranking));

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        globalRankingService.distributeTournamentPoints(
                tournament,
                category,
                Map.of(player, 1)
        );

        assertEquals(150, ranking.getTotalPoints());
        assertEquals(3, ranking.getTournamentsPlayed());

        verify(rankingRepository).save(ranking);
    }

    @Test
    void shouldNotDistributePointsWhenScoringConfigDoesNotExist() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);
        Player player = createPlayer(1L, "Player");

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> globalRankingService.distributeTournamentPoints(
                        tournament,
                        category,
                        Map.of(player, 1)
                )
        );

        assertEquals(
                "Configuração de pontuação não encontrada para este torneio.",
                exception.getMessage()
        );

        verify(rankingRepository, never())
                .save(any(PlayerGlobalRanking.class));
    }

    @Test
    void shouldProcessTournamentCompletionAndDistributeChampionAndRunnerUpPoints() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");

        Match finalMatch = createMatch(
                category,
                champion,
                runnerUp,
                MatchPhase.FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);
        config.setPointsRunnerUp(70);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(finalMatch)
        );

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 70, 1);
    }

    @Test
    void shouldProcessTournamentCompletionWhenFinalIsFinishedByWalkover() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");

        Match finalMatch = createMatch(
                category,
                champion,
                runnerUp,
                MatchPhase.FINAL,
                MatchStatus.WO,
                2,
                0
        );

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);
        config.setPointsRunnerUp(70);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(finalMatch)
        );

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 70, 1);
        assertEquals(true, category.isRankingProcessed());
    }

    @Test
    void shouldProcessSemiFinalAndQuarterFinalPositions() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");
        Player third = createPlayer(3L, "Third");
        Player fourth = createPlayer(4L, "Fourth");
        Player fifth = createPlayer(5L, "Fifth");
        Player sixth = createPlayer(6L, "Sixth");

        Match finalMatch = createMatch(
                category,
                champion,
                runnerUp,
                MatchPhase.FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match semi1 = createMatch(
                category,
                champion,
                third,
                MatchPhase.SEMI_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match semi2 = createMatch(
                category,
                runnerUp,
                fourth,
                MatchPhase.SEMI_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match quarter1 = createMatch(
                category,
                champion,
                fifth,
                MatchPhase.QUARTER_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match quarter2 = createMatch(
                category,
                runnerUp,
                sixth,
                MatchPhase.QUARTER_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);
        config.setPointsRunnerUp(70);
        config.setPointsSemiFinalsLoser(50);
        config.setPointsQuarterFinalsLoser(30);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(
                        finalMatch,
                        semi1,
                        semi2,
                        quarter1,
                        quarter2
                )
        );

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 70, 1);
        verifyRankingSaved(third, 50, 1);
        verifyRankingSaved(fourth, 50, 1);
        verifyRankingSaved(fifth, 30, 1);
        verifyRankingSaved(sixth, 30, 1);
    }

    @Test
    void shouldDistributeSameByPositionPointsForPlayersEliminatedInSameRound() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");
        Player third = createPlayer(3L, "Third");
        Player fourth = createPlayer(4L, "Fourth");
        Player fifth = createPlayer(5L, "Fifth");
        Player sixth = createPlayer(6L, "Sixth");
        Player seventh = createPlayer(7L, "Seventh");
        Player eighth = createPlayer(8L, "Eighth");

        Match finalMatch = createMatch(
                category, champion, runnerUp,
                MatchPhase.FINAL, MatchStatus.FINISHED, 3, 1);
        Match semi1 = createMatch(
                category, champion, third,
                MatchPhase.SEMI_FINAL, MatchStatus.FINISHED, 3, 1);
        Match semi2 = createMatch(
                category, runnerUp, fourth,
                MatchPhase.SEMI_FINAL, MatchStatus.FINISHED, 3, 1);
        Match quarter1 = createMatch(
                category, champion, fifth,
                MatchPhase.QUARTER_FINAL, MatchStatus.FINISHED, 3, 1);
        Match quarter2 = createMatch(
                category, runnerUp, sixth,
                MatchPhase.QUARTER_FINAL, MatchStatus.FINISHED, 3, 1);
        Match quarter3 = createMatch(
                category, third, seventh,
                MatchPhase.QUARTER_FINAL, MatchStatus.FINISHED, 3, 1);
        Match quarter4 = createMatch(
                category, fourth, eighth,
                MatchPhase.QUARTER_FINAL, MatchStatus.FINISHED, 3, 1);

        TournamentScoringConfig config = createByPositionConfig();
        config.setPoints1stPlace(100);
        config.setPoints2ndPlace(80);
        config.setPoints3rdPlace(60);
        config.setPoints4thPlace(50);
        config.setPoints5thPlace(40);
        config.setPoints6thPlace(30);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(finalMatch, semi2, semi1, quarter1, quarter2, quarter3, quarter4));

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 80, 1);
        // O campeão (1º) define o 3º: seu adversário eliminado na semifinal.
        verifyRankingSaved(third, 60, 1);
        // O vice-campeão (2º) define o 4º: seu adversário eliminado na semifinal.
        verifyRankingSaved(fourth, 50, 1);

        // Os dois eliminados nas quartas compartilham o 5º lugar.
        verifyRankingSaved(fifth, 40, 1);
        verifyRankingSaved(sixth, 40, 1);
        verifyRankingSaved(seventh, 40, 1);
        verifyRankingSaved(eighth, 40, 1);
    }

    @Test
    void shouldUseConfiguredClassicPointsForRoundOf16AndRoundOf32Losers() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");
        Player eliminatedInRoundOf16 = createPlayer(3L, "Round Of 16 Loser");
        Player eliminatedInRoundOf32 = createPlayer(4L, "Round Of 32 Loser");

        Match finalMatch = createMatch(
                category, champion, runnerUp,
                MatchPhase.FINAL, MatchStatus.FINISHED, 3, 1);
        Match roundOf16Match = createMatch(
                category, createPlayer(5L, "Round Of 16 Winner"), eliminatedInRoundOf16,
                MatchPhase.ROUND_OF_16, MatchStatus.FINISHED, 3, 1);
        Match roundOf32Match = createMatch(
                category, createPlayer(6L, "Round Of 32 Winner"), eliminatedInRoundOf32,
                MatchPhase.ROUND_OF_32, MatchStatus.FINISHED, 3, 1);

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);
        config.setPointsRunnerUp(80);
        config.setPointsRoundOf16Loser(50);
        config.setPointsRoundOf32Loser(30);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(finalMatch, roundOf16Match, roundOf32Match));

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 80, 1);
        verifyRankingSaved(eliminatedInRoundOf16, 50, 1);
        verifyRankingSaved(eliminatedInRoundOf32, 30, 1);
    }

    @Test
    void shouldDistributeNinthAndSeventeenthPlacePointsByEliminationRound() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");
        Player ninthPlace = createPlayer(3L, "Ninth Place");
        Player seventeenthPlace = createPlayer(4L, "Seventeenth Place");

        Match finalMatch = createMatch(
                category, champion, runnerUp,
                MatchPhase.FINAL, MatchStatus.FINISHED, 3, 1);
        Match roundOf16Match = createMatch(
                category, createPlayer(5L, "Round Of 16 Winner"), ninthPlace,
                MatchPhase.ROUND_OF_16, MatchStatus.FINISHED, 3, 1);
        Match roundOf32Match = createMatch(
                category, createPlayer(6L, "Round Of 32 Winner"), seventeenthPlace,
                MatchPhase.ROUND_OF_32, MatchStatus.FINISHED, 3, 1);

        TournamentScoringConfig config = createByPositionConfig();
        config.setPoints1stPlace(100);
        config.setPoints2ndPlace(90);
        config.setPoints9thAndBeyond(10);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(finalMatch, roundOf16Match, roundOf32Match));

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 90, 1);
        verifyRankingSaved(ninthPlace, 10, 1);
        verifyRankingSaved(seventeenthPlace, 10, 1);
    }

    @Test
    void shouldUseGroupStagePointsForPlayersEliminatedInGroupStage() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");
        Player eliminatedInGroups = createPlayer(3L, "Group Stage Loser");
        Player groupOpponent = createPlayer(4L, "Group Opponent");

        Match finalMatch = createMatch(
                category, champion, runnerUp,
                MatchPhase.FINAL, MatchStatus.FINISHED, 3, 1);
        Match groupMatch = createMatch(
                category, groupOpponent, eliminatedInGroups,
                MatchPhase.GROUP, MatchStatus.FINISHED, 3, 0);

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);
        config.setPointsRunnerUp(80);
        config.setPointsGroupStageLoser(15);
        config.setPointsRoundOf16Loser(50);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(finalMatch, groupMatch));

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 80, 1);
        verifyRankingSaved(eliminatedInGroups, 15, 1);
    }

    @Test
    void shouldUseConfiguredNinthAndBeyondPointsForGroupStageEliminationsInByPositionSystem() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");
        Player eliminatedInGroups = createPlayer(3L, "Group Stage Loser");
        Player groupOpponent = createPlayer(4L, "Group Opponent");

        Match finalMatch = createMatch(
                category, champion, runnerUp,
                MatchPhase.FINAL, MatchStatus.FINISHED, 3, 1);
        Match groupMatch = createMatch(
                category, groupOpponent, eliminatedInGroups,
                MatchPhase.GROUP, MatchStatus.FINISHED, 3, 0);

        TournamentScoringConfig config = createByPositionConfig();
        config.setPoints1stPlace(100);
        config.setPoints2ndPlace(90);
        config.setPoints9thAndBeyond(10);

        mockScoringConfig(config);
        mockEmptyRankings();

        globalRankingService.processTournamentCompletion(
                tournament, List.of(finalMatch, groupMatch));

        verifyRankingSaved(champion, 100, 1);
        verifyRankingSaved(runnerUp, 90, 1);
        verifyRankingSaved(eliminatedInGroups, 10, 1);
    }

    @Test
    void shouldNotDistributePointsWhenFinalIsInvalid() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match invalidFinal = createMatch(
                category,
                player1,
                player2,
                MatchPhase.FINAL,
                MatchStatus.SCHEDULED,
                3,
                1
        );

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(invalidFinal)
        );

        verify(scoringConfigRepository, never())
                .findByTournamentId(1L);

        verify(rankingRepository, never())
                .save(any(PlayerGlobalRanking.class));
    }

    @Test
    void shouldNotDistributePointsAgainWhenCategoryWasAlreadyProcessed() {
        Tournament tournament = createTournament(1L);
        Category category = createCategory(1L, tournament);
        category.setRankingProcessed(true);

        Player player = createPlayer(1L, "Player");

        globalRankingService.distributeTournamentPoints(
                tournament,
                category,
                Map.of(player, 1)
        );

        verify(scoringConfigRepository, never())
                .findByTournamentId(1L);

        verify(rankingRepository, never())
                .save(any(PlayerGlobalRanking.class));
    }

    private void mockScoringConfig(TournamentScoringConfig config) {
        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));
    }

    private void mockEmptyRankings() {
        when(rankingRepository.findByPlayerIdAndCategoryIdAndYear(
                any(Long.class),
                any(Long.class),
                any(Integer.class)
        )).thenReturn(Optional.empty());

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void verifyRankingSaved(
            Player player,
            int expectedPoints,
            int expectedTournamentsPlayed) {

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(player)
                                && ranking.getTotalPoints() == expectedPoints
                                && ranking.getTournamentsPlayed()
                                        == expectedTournamentsPlayed
                )
        );
    }

    private Tournament createTournament(Long id) {
        Tournament tournament = new Tournament();
        tournament.setId(id);
        tournament.setName("Tournament Test");
        return tournament;
    }

    private Category createCategory(Long id, Tournament tournament) {
        Category category = new Category();
        category.setId(id);
        category.setTournament(tournament);
        return category;
    }

    private Player createPlayer(Long id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setEmail(
                name.toLowerCase().replace(" ", "") + "@email.com"
        );
        player.setBirthDate(LocalDate.of(2010, 1, 1));
        return player;
    }

    private TournamentScoringConfig createClassicConfig() {
        TournamentScoringConfig config = new TournamentScoringConfig();
        config.setSystemType(ScoringSystemType.CLASSIC);
        return config;
    }

    private TournamentScoringConfig createByPositionConfig() {
        TournamentScoringConfig config = new TournamentScoringConfig();
        config.setSystemType(ScoringSystemType.BY_POSITION);
        return config;
    }

    private Match createMatch(
            Category category,
            Player player1,
            Player player2,
            MatchPhase phase,
            MatchStatus status,
            int scorePlayer1,
            int scorePlayer2) {

        Match match = new Match();
        match.setCategory(category);
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setPhase(phase);
        match.setStatus(status);
        match.setScorePlayer1(scorePlayer1);
        match.setScorePlayer2(scorePlayer2);

        return match;
    }
}