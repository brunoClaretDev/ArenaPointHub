package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;

import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.TournamentScoringConfig;
import com.arenapointhub.api.model.Administrator;

@DataJpaTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = AutowireMode.ALL)
class TournamentScoringConfigRepositoryTest {

    private final TournamentScoringConfigRepository scoringConfigRepository;
    private final TournamentRepository tournamentRepository;
    private final AdministratorRepository administratorRepository;

    TournamentScoringConfigRepositoryTest(
            TournamentScoringConfigRepository scoringConfigRepository,
            TournamentRepository tournamentRepository,
            AdministratorRepository administratorRepository) {

        this.scoringConfigRepository = scoringConfigRepository;
        this.tournamentRepository = tournamentRepository;
        this.administratorRepository = administratorRepository;
    }

    @Test
    void shouldFindScoringConfigByTournamentId() {
    	Administrator administrator = new Administrator();
    	administrator.setUsername("admin-scoring-teste");
    	administrator.setPasswordHash("test-password");
    	administrator.setActive(true);

    	Administrator savedAdministrator =
    	        administratorRepository.save(administrator);
    	
    	Tournament tournament = new Tournament();
    	tournament.setName("Torneio Teste");
    	tournament.setAdministrator(savedAdministrator);

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