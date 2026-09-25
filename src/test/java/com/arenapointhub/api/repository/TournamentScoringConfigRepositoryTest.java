package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;

import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.TournamentScoringConfig;

@DataJpaTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class TournamentScoringConfigRepositoryTest {

    private final TournamentScoringConfigRepository scoringConfigRepository;
    private final TournamentRepository tournamentRepository;

    TournamentScoringConfigRepositoryTest(
            TournamentScoringConfigRepository scoringConfigRepository,
            TournamentRepository tournamentRepository) {

        this.scoringConfigRepository = scoringConfigRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Test
    void shouldFindScoringConfigByTournamentId() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        TournamentScoringConfig config =
                new TournamentScoringConfig();

        config.setTournament(savedTournament);
        config.setPointsChampion(100);
        config.setPointsRunnerUp(70);

        TournamentScoringConfig savedConfig =
                scoringConfigRepository.save(config);

        Optional<TournamentScoringConfig> foundConfig =
                scoringConfigRepository.findByTournamentId(
                        savedTournament.getId());

        assertTrue(foundConfig.isPresent());
        assertEquals(
                savedConfig.getId(),
                foundConfig.get().getId());
        assertEquals(
                savedTournament.getId(),
                foundConfig.get().getTournament().getId());
        assertEquals(
                100,
                foundConfig.get().getPointsChampion());
        assertEquals(
                70,
                foundConfig.get().getPointsRunnerUp());
    }
}