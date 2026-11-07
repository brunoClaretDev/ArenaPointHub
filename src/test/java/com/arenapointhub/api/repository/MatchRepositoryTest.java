
package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;

@DataJpaTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MatchRepositoryTest {

    private final MatchRepository matchRepository;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final GroupRepository groupRepository;
    private final AdministratorRepository administratorRepository;

    MatchRepositoryTest(
            MatchRepository matchRepository,
            CategoryRepository categoryRepository,
            TournamentRepository tournamentRepository,
            PlayerRepository playerRepository,
            GroupRepository groupRepository,
            AdministratorRepository administratorRepository) {

        this.matchRepository = matchRepository;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.groupRepository = groupRepository;
        this.administratorRepository = administratorRepository;
    }

    @Test
    void shouldFindMatchesByCategoryId() {
        Category savedCategory = createSavedCategory("admin-match-test");

        Match match1 = new Match();
        match1.setCategory(savedCategory);

        Match match2 = new Match();
        match2.setCategory(savedCategory);

        matchRepository.save(match1);
        matchRepository.save(match2);

        List<Match> matches =
                matchRepository.findByCategoryId(savedCategory.getId());

        assertEquals(2, matches.size());
        assertEquals(savedCategory.getId(), matches.get(0).getCategory().getId());
        assertEquals(savedCategory.getId(), matches.get(1).getCategory().getId());
    }

    @Test
    void shouldFindMatchesByPlayer() {
        Category savedCategory = createSavedCategory("admin-player-test");

        Player savedPlayer1 = createSavedPlayer(
                "Jogador 1", "jogador1@email.com");

        Player savedPlayer2 = createSavedPlayer(
                "Jogador 2", "jogador2@email.com");

        Match match = new Match();
        match.setCategory(savedCategory);
        match.setPlayer1(savedPlayer1);
        match.setPlayer2(savedPlayer2);

        matchRepository.save(match);

        List<Match> matches =
                matchRepository.findByPlayer1IdOrPlayer2Id(
                        savedPlayer1.getId(),
                        savedPlayer1.getId());

        assertEquals(1, matches.size());
        assertEquals(savedPlayer1.getId(), matches.get(0).getPlayer1().getId());
    }

    @Test
    void shouldFindMatchesByGroupId() {
        Category savedCategory = createSavedCategory("admin-group-test");

        Group group = new Group();
        group.setName("Grupo A");
        group.setCategory(savedCategory);

        Group savedGroup = groupRepository.save(group);

        Match match1 = new Match();
        match1.setCategory(savedCategory);
        match1.setGroup(savedGroup);

        Match match2 = new Match();
        match2.setCategory(savedCategory);
        match2.setGroup(savedGroup);

        matchRepository.save(match1);
        matchRepository.save(match2);

        List<Match> matches =
                matchRepository.findByGroupId(savedGroup.getId());

        assertEquals(2, matches.size());
        assertEquals(savedGroup.getId(), matches.get(0).getGroup().getId());
        assertEquals(savedGroup.getId(), matches.get(1).getGroup().getId());
    }

    @Test
    void shouldDeleteMatchesByCategoryAndPhase() {
        Category savedCategory = createSavedCategory("admin-delete-test");

        Match groupMatch = new Match();
        groupMatch.setCategory(savedCategory);
        groupMatch.setPhase(MatchPhase.GROUP);

        Match playoffMatch = new Match();
        playoffMatch.setCategory(savedCategory);
        playoffMatch.setPhase(MatchPhase.QUARTER_FINAL);

        matchRepository.save(groupMatch);
        matchRepository.save(playoffMatch);

        matchRepository.deleteByCategoryIdAndPhaseNot(
                savedCategory.getId(),
                MatchPhase.GROUP);

        List<Match> matches =
                matchRepository.findByCategoryId(savedCategory.getId());

        assertEquals(1, matches.size());
        assertEquals(MatchPhase.GROUP, matches.get(0).getPhase());
    }

    @Test
    void shouldCheckIfTableOrCourtIsOccupied() {
        Category savedCategory = createSavedCategory("admin-table-test");

        Match match = new Match();
        match.setCategory(savedCategory);
        match.setTableOrCourt("Mesa 1");
        match.setScheduledTime("14:00");
        match.setStatus(MatchStatus.SCHEDULED);

        matchRepository.save(match);

        boolean occupied =
                matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                        "Mesa 1",
                        "14:00",
                        MatchStatus.CANCELED);

        assertTrue(occupied);
    }

    @Test
    void shouldCheckIfPlayerIsBusy() {
        Category savedCategory = createSavedCategory("admin-busy-test");

        Player savedPlayer1 = createSavedPlayer(
                "Jogador 1", "jogador1.busy@email.com");

        Player savedPlayer2 = createSavedPlayer(
                "Jogador 2", "jogador2.busy@email.com");

        Match match = new Match();
        match.setCategory(savedCategory);
        match.setPlayer1(savedPlayer1);
        match.setPlayer2(savedPlayer2);
        match.setScheduledTime("14:00");
        match.setStatus(MatchStatus.SCHEDULED);

        matchRepository.save(match);

        boolean playerBusy =
                matchRepository.existsByPlayerBusy(
                        savedPlayer1.getId(),
                        savedPlayer2.getId(),
                        "14:00",
                        MatchStatus.CANCELED);

        assertTrue(playerBusy);
    }

    @Test
    void shouldFindMatchesByAdministratorId() {
        Category category1 = createSavedCategory("admin-one-test");
        Category category2 = createSavedCategory("admin-two-test");

        matchRepository.save(createMatch(category1));
        matchRepository.save(createMatch(category2));

        Long administratorId =
                category1.getTournament().getAdministrator().getId();

        List<Match> matches =
                matchRepository.findByCategory_Tournament_Administrator_Id(
                        administratorId);

        assertEquals(1, matches.size());
        assertEquals(category1.getId(), matches.get(0).getCategory().getId());
    }

    @Test
    void shouldFindMatchesByCategoryAndAdministratorId() {
        Category category1 = createSavedCategory("admin-category-one");
        Category category2 = createSavedCategory("admin-category-two");

        matchRepository.save(createMatch(category1));
        matchRepository.save(createMatch(category2));

        Long administratorId =
                category1.getTournament().getAdministrator().getId();

        List<Match> matches =
                matchRepository.findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        category1.getId(),
                        administratorId);

        assertEquals(1, matches.size());
        assertEquals(category1.getId(), matches.get(0).getCategory().getId());
    }

    @Test
    void shouldFindMatchByIdAndAdministratorId() {
        Category category = createSavedCategory("admin-match-id-test");

        Match savedMatch = matchRepository.save(createMatch(category));

        Long administratorId =
                category.getTournament().getAdministrator().getId();

        var result =
                matchRepository.findByIdAndCategory_Tournament_Administrator_Id(
                        savedMatch.getId(),
                        administratorId);

        assertTrue(result.isPresent());
        assertEquals(savedMatch.getId(), result.get().getId());
    }

    @Test
    void shouldNotFindMatchByIdForAnotherAdministrator() {
        Category category1 = createSavedCategory("admin-owner-test");
        Category category2 = createSavedCategory("admin-other-test");

        Match savedMatch = matchRepository.save(createMatch(category1));

        Long otherAdministratorId =
                category2.getTournament().getAdministrator().getId();

        var result =
                matchRepository.findByIdAndCategory_Tournament_Administrator_Id(
                        savedMatch.getId(),
                        otherAdministratorId);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindMatchesByStatusAndAdministratorId() {
        Category category1 = createSavedCategory("admin-status-one");
        Category category2 = createSavedCategory("admin-status-two");

        Match scheduledMatch = createMatch(category1);
        scheduledMatch.setStatus(MatchStatus.SCHEDULED);

        Match finishedMatch = createMatch(category1);
        finishedMatch.setStatus(MatchStatus.FINISHED);

        Match otherAdminMatch = createMatch(category2);
        otherAdminMatch.setStatus(MatchStatus.SCHEDULED);

        matchRepository.save(scheduledMatch);
        matchRepository.save(finishedMatch);
        matchRepository.save(otherAdminMatch);

        Long administratorId =
                category1.getTournament().getAdministrator().getId();

        List<Match> matches =
                matchRepository.findByStatusAndCategory_Tournament_Administrator_Id(
                        MatchStatus.SCHEDULED,
                        administratorId);

        assertEquals(1, matches.size());
        assertEquals(MatchStatus.SCHEDULED, matches.get(0).getStatus());
        assertEquals(category1.getId(), matches.get(0).getCategory().getId());
    }

    @Test
    void shouldFindMatchesByPlayerAndAdministratorId() {
        Category category1 = createSavedCategory("admin-player-one");
        Category category2 = createSavedCategory("admin-player-two");

        Player player = createSavedPlayer(
                "Jogador Compartilhado",
                "jogador.compartilhado@email.com");

        Match match1 = createMatch(category1);
        match1.setPlayer1(player);

        Match match2 = createMatch(category2);
        match2.setPlayer1(player);

        matchRepository.save(match1);
        matchRepository.save(match2);

        Long administratorId =
                category1.getTournament().getAdministrator().getId();

        List<Match> matches =
                matchRepository.findByPlayerIdAndAdministratorId(
                        player.getId(),
                        administratorId);

        assertEquals(1, matches.size());
        assertEquals(category1.getId(), matches.get(0).getCategory().getId());
        assertEquals(player.getId(), matches.get(0).getPlayer1().getId());
    }

    private Category createSavedCategory(String username) {
        Administrator administrator = new Administrator();
        administrator.setUsername(username);
        administrator.setPasswordHash("test-password");
        administrator.setActive(true);

        Administrator savedAdministrator =
                administratorRepository.save(administrator);

        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");
        tournament.setAdministrator(savedAdministrator);

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        return categoryRepository.save(category);
    }

    private Player createSavedPlayer(String name, String email) {
        Player player = new Player();
        player.setName(name);
        player.setEmail(email);
        player.setBirthDate(LocalDate.of(2012, 5, 10));

        return playerRepository.save(player);
    }

    private Match createMatch(Category category) {
        Match match = new Match();
        match.setCategory(category);

        return match;
    }
}
