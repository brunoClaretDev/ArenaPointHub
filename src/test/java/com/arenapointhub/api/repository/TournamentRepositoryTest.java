package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.model.Tournament;

@DataJpaTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TournamentRepositoryTest {

    private final TournamentRepository tournamentRepository;
    private final AdministratorRepository administratorRepository;

    TournamentRepositoryTest(
            TournamentRepository tournamentRepository,
            AdministratorRepository administratorRepository) {
        this.tournamentRepository = tournamentRepository;
        this.administratorRepository = administratorRepository;
    }

    @Test
    void shouldSaveAndFindTournament() {
        Administrator administrator =
                createAdministrator("admin-teste");

        Tournament tournament =
                createTournament("Torneio Teste", administrator);

        Optional<Tournament> foundTournament =
                tournamentRepository.findById(tournament.getId());

        assertTrue(foundTournament.isPresent());
        assertEquals("Torneio Teste", foundTournament.get().getName());
        assertEquals(
                administrator.getId(),
                foundTournament.get().getAdministrator().getId());
    }

    @Test
    void shouldFindTournamentsByAdministrator() {
        Administrator firstAdministrator =
                createAdministrator("admin1");

        Administrator secondAdministrator =
                createAdministrator("admin2");

        Tournament firstTournament =
                createTournament("Campeonato 1", firstAdministrator);

        Tournament secondTournament =
                createTournament("Campeonato 2", firstAdministrator);

        createTournament("Campeonato 3", secondAdministrator);

        List<Tournament> tournaments =
                tournamentRepository.findAllByAdministrator_Id(
                        firstAdministrator.getId());

        assertEquals(2, tournaments.size());

        assertTrue(tournaments.stream()
                .anyMatch(tournament ->
                        tournament.getId().equals(firstTournament.getId())));

        assertTrue(tournaments.stream()
                .anyMatch(tournament ->
                        tournament.getId().equals(secondTournament.getId())));
    }

    @Test
    void shouldFindTournamentByIdAndAdministrator() {
        Administrator administrator =
                createAdministrator("admin1");

        Tournament tournament =
                createTournament("Campeonato 1", administrator);

        Optional<Tournament> foundTournament =
                tournamentRepository.findByIdAndAdministrator_Id(
                        tournament.getId(),
                        administrator.getId());

        assertTrue(foundTournament.isPresent());
    }

    @Test
    void shouldNotFindTournamentOwnedByAnotherAdministrator() {
        Administrator owner =
                createAdministrator("admin1");

        Administrator anotherAdministrator =
                createAdministrator("admin2");

        Tournament tournament =
                createTournament("Campeonato 1", owner);

        Optional<Tournament> foundTournament =
                tournamentRepository.findByIdAndAdministrator_Id(
                        tournament.getId(),
                        anotherAdministrator.getId());

        assertFalse(foundTournament.isPresent());
    }

    @Test
    void shouldCheckTournamentOwnership() {
        Administrator owner =
                createAdministrator("admin1");

        Administrator anotherAdministrator =
                createAdministrator("admin2");

        Tournament tournament =
                createTournament("Campeonato 1", owner);

        assertTrue(tournamentRepository.existsByIdAndAdministrator_Id(
                tournament.getId(),
                owner.getId()));

        assertFalse(tournamentRepository.existsByIdAndAdministrator_Id(
                tournament.getId(),
                anotherAdministrator.getId()));
    }

    private Administrator createAdministrator(String username) {
        Administrator administrator = new Administrator();
        administrator.setUsername(username);
        administrator.setPasswordHash("test-password");
        administrator.setActive(true);

        return administratorRepository.save(administrator);
    }

    private Tournament createTournament(
            String name,
            Administrator administrator) {

        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setAdministrator(administrator);

        return tournamentRepository.save(tournament);
    }
}