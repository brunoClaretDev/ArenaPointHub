package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Tournament;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TournamentRepositoryTest {

    private final TournamentRepository tournamentRepository;

    TournamentRepositoryTest(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Test
    void shouldSaveAndFindTournament() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Optional<Tournament> foundTournament =
                tournamentRepository.findById(savedTournament.getId());

        assertTrue(foundTournament.isPresent());
        assertEquals(
                "Torneio Teste",
                foundTournament.get().getName());
    }
}