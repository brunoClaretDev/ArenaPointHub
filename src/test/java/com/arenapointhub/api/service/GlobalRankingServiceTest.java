package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.exception.BusinessException;
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

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);
        config.setPointsRunnerUp(70);

        Map<Player, Integer> positions = new HashMap<>();
        positions.put(champion, 1);
        positions.put(runnerUp, 2);

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        when(rankingRepository.findByPlayerIdAndYear(
                any(Long.class),
                any(Integer.class)))
                .thenReturn(Optional.empty());

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        globalRankingService.distributeTournamentPoints(
                tournament,
                positions
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(champion)
                                && ranking.getTotalPoints() == 100
                                && ranking.getTournamentsPlayed() == 1
                )
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(runnerUp)
                                && ranking.getTotalPoints() == 70
                                && ranking.getTournamentsPlayed() == 1
                )
        );
    }

    @Test
    void shouldDistributeByPositionPoints() {
        Tournament tournament = createTournament(1L);

        Player player = createPlayer(1L, "Player");

        TournamentScoringConfig config = createByPositionConfig();
        config.setPoints1stPlace(150);

        Map<Player, Integer> positions = new HashMap<>();
        positions.put(player, 1);

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        when(rankingRepository.findByPlayerIdAndYear(
                any(Long.class),
                any(Integer.class)))
                .thenReturn(Optional.empty());

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        globalRankingService.distributeTournamentPoints(
                tournament,
                positions
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(player)
                                && ranking.getTotalPoints() == 150
                                && ranking.getTournamentsPlayed() == 1
                )
        );
    }

    @Test
    void shouldCreateRankingWhenPlayerHasNoRankingForCurrentYear() {
        Tournament tournament = createTournament(1L);
        Player player = createPlayer(1L, "Player");

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);

        Map<Player, Integer> positions = Map.of(player, 1);

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        when(rankingRepository.findByPlayerIdAndYear(
                1L,
                LocalDate.now().getYear()))
                .thenReturn(Optional.empty());

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        globalRankingService.distributeTournamentPoints(
                tournament,
                positions
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(player)
                                && ranking.getYear() == LocalDate.now().getYear()
                                && ranking.getTotalPoints() == 100
                                && ranking.getTournamentsPlayed() == 1
                )
        );
    }

    @Test
    void shouldUpdateExistingRanking() {
        Tournament tournament = createTournament(1L);
        Player player = createPlayer(1L, "Player");

        TournamentScoringConfig config = createClassicConfig();
        config.setPointsChampion(100);

        PlayerGlobalRanking ranking = new PlayerGlobalRanking();
        ranking.setPlayer(player);
        ranking.setYear(LocalDate.now().getYear());
        ranking.setTotalPoints(50);
        ranking.setTournamentsPlayed(2);

        Map<Player, Integer> positions = Map.of(player, 1);

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        when(rankingRepository.findByPlayerIdAndYear(
                1L,
                LocalDate.now().getYear()))
                .thenReturn(Optional.of(ranking));

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        globalRankingService.distributeTournamentPoints(
                tournament,
                positions
        );

        assertEquals(150, ranking.getTotalPoints());
        assertEquals(3, ranking.getTournamentsPlayed());

        verify(rankingRepository).save(ranking);
    }

    @Test
    void shouldNotDistributePointsWhenScoringConfigDoesNotExist() {
        Tournament tournament = createTournament(1L);
        Player player = createPlayer(1L, "Player");

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> globalRankingService.distributeTournamentPoints(
                        tournament,
                        Map.of(player, 1)
                )
        );

        assertEquals(
                "Configuração de pontuação não encontrada para este torneio.",
                exception.getMessage()
        );

        verify(rankingRepository, never()).save(any(PlayerGlobalRanking.class));
    }

    @Test
    void shouldProcessTournamentCompletionAndDistributeChampionAndRunnerUpPoints() {
        Tournament tournament = createTournament(1L);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");

        Match finalMatch = createMatch(
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

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        when(rankingRepository.findByPlayerIdAndYear(
                any(Long.class),
                any(Integer.class)))
                .thenReturn(Optional.empty());

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(finalMatch)
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(champion)
                                && ranking.getTotalPoints() == 100
                )
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(runnerUp)
                                && ranking.getTotalPoints() == 70
                )
        );
    }

    @Test
    void shouldProcessSemiFinalAndQuarterFinalPositions() {
        Tournament tournament = createTournament(1L);

        Player champion = createPlayer(1L, "Champion");
        Player runnerUp = createPlayer(2L, "Runner Up");
        Player third = createPlayer(3L, "Third");
        Player fourth = createPlayer(4L, "Fourth");
        Player fifth = createPlayer(5L, "Fifth");
        Player sixth = createPlayer(6L, "Sixth");

        Match finalMatch = createMatch(
                champion,
                runnerUp,
                MatchPhase.FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match semi1 = createMatch(
                champion,
                third,
                MatchPhase.SEMI_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match semi2 = createMatch(
                runnerUp,
                fourth,
                MatchPhase.SEMI_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match quarter1 = createMatch(
                champion,
                fifth,
                MatchPhase.QUARTER_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match quarter2 = createMatch(
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

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        when(rankingRepository.findByPlayerIdAndYear(
                any(Long.class),
                any(Integer.class)))
                .thenReturn(Optional.empty());

        when(rankingRepository.save(any(PlayerGlobalRanking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(champion)
                                && ranking.getTotalPoints() == 100
                )
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(runnerUp)
                                && ranking.getTotalPoints() == 70
                )
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(third)
                                && ranking.getTotalPoints() == 50
                )
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(fourth)
                                && ranking.getTotalPoints() == 50
                )
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(fifth)
                                && ranking.getTotalPoints() == 30
                )
        );

        verify(rankingRepository).save(
                org.mockito.ArgumentMatchers.argThat(ranking ->
                        ranking.getPlayer().equals(sixth)
                                && ranking.getTotalPoints() == 30
                )
        );
    }

    @Test
    void shouldNotDistributePointsWhenFinalIsInvalid() {
        Tournament tournament = createTournament(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match invalidFinal = createMatch(
                player1,
                player2,
                MatchPhase.FINAL,
                MatchStatus.SCHEDULED,
                3,
                1
        );

        TournamentScoringConfig config = createClassicConfig();

        when(scoringConfigRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        globalRankingService.processTournamentCompletion(
                tournament,
                List.of(invalidFinal)
        );

        verify(scoringConfigRepository).findByTournamentId(1L);
        verify(rankingRepository, never()).save(any(PlayerGlobalRanking.class));
    }

    private Tournament createTournament(Long id) {
        Tournament tournament = new Tournament();
        tournament.setId(id);
        tournament.setName("Tournament Test");
        return tournament;
    }

    private Player createPlayer(Long id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setEmail(name.toLowerCase().replace(" ", "") + "@email.com");
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
            Player player1,
            Player player2,
            MatchPhase phase,
            MatchStatus status,
            int scorePlayer1,
            int scorePlayer2) {

        Match match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setPhase(phase);
        match.setStatus(status);
        match.setScorePlayer1(scorePlayer1);
        match.setScorePlayer2(scorePlayer2);

        return match;
    }
}