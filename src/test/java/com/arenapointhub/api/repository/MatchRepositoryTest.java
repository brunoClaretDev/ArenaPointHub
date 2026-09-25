package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MatchRepositoryTest {

    private final MatchRepository matchRepository;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final GroupRepository groupRepository;

    MatchRepositoryTest(
            MatchRepository matchRepository,
            CategoryRepository categoryRepository,
            TournamentRepository tournamentRepository,
            PlayerRepository playerRepository,
            GroupRepository groupRepository) {

        this.matchRepository = matchRepository;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.groupRepository = groupRepository;
    }

    @Test
    void shouldFindMatchesByCategoryId() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory =
                categoryRepository.save(category);

        Match match1 = new Match();
        match1.setCategory(savedCategory);

        Match match2 = new Match();
        match2.setCategory(savedCategory);

        matchRepository.save(match1);
        matchRepository.save(match2);

        List<Match> matches =
                matchRepository.findByCategoryId(savedCategory.getId());

        assertEquals(2, matches.size());
        assertEquals(savedCategory.getId(),
                matches.get(0).getCategory().getId());
        assertEquals(savedCategory.getId(),
                matches.get(1).getCategory().getId());
    }
    
    @Test
    void shouldFindMatchesByStatus() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory =
                categoryRepository.save(category);

        Match scheduledMatch = new Match();
        scheduledMatch.setCategory(savedCategory);
        scheduledMatch.setStatus(MatchStatus.SCHEDULED);

        Match finishedMatch = new Match();
        finishedMatch.setCategory(savedCategory);
        finishedMatch.setStatus(MatchStatus.FINISHED);

        matchRepository.save(scheduledMatch);
        matchRepository.save(finishedMatch);

        List<Match> matches =
                matchRepository.findByStatus(MatchStatus.SCHEDULED);

        assertEquals(1, matches.size());
        assertEquals(
                MatchStatus.SCHEDULED,
                matches.get(0).getStatus());
    }
    
    @Test
    void shouldFindMatchesByPlayer() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory =
                categoryRepository.save(category);

        Player player1 = new Player();
        player1.setName("Jogador 1");
        player1.setEmail("jogador1@email.com");
        player1.setBirthDate(LocalDate.of(2012, 5, 10));

        Player player2 = new Player();
        player2.setName("Jogador 2");
        player2.setEmail("jogador2@email.com");
        player2.setBirthDate(LocalDate.of(2012, 6, 15));

        Player savedPlayer1 = playerRepository.save(player1);
        Player savedPlayer2 = playerRepository.save(player2);

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
        assertEquals(
                savedPlayer1.getId(),
                matches.get(0).getPlayer1().getId());
    }
    
    @Test
    void shouldFindMatchesByGroupId() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory =
                categoryRepository.save(category);

        Group group = new Group();
        group.setName("Grupo A");
        group.setCategory(savedCategory);

        Group savedGroup =
                groupRepository.save(group);

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
        assertEquals(
                savedGroup.getId(),
                matches.get(0).getGroup().getId());
        assertEquals(
                savedGroup.getId(),
                matches.get(1).getGroup().getId());
    }
    
    @Test
    void shouldDeleteMatchesByCategoryAndPhase() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory =
                categoryRepository.save(category);

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
        assertEquals(
                MatchPhase.GROUP,
                matches.get(0).getPhase());
    }
    
    @Test
    void shouldCheckIfTableOrCourtIsOccupied() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory =
                categoryRepository.save(category);

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

        assertEquals(true, occupied);
    }
    
    @Test
    void shouldCheckIfPlayerIsBusy() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament =
                tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory =
                categoryRepository.save(category);

        Player player1 = new Player();
        player1.setName("Jogador 1");
        player1.setEmail("jogador1.busy@email.com");
        player1.setBirthDate(LocalDate.of(2012, 5, 10));

        Player player2 = new Player();
        player2.setName("Jogador 2");
        player2.setEmail("jogador2.busy@email.com");
        player2.setBirthDate(LocalDate.of(2012, 6, 15));

        Player savedPlayer1 = playerRepository.save(player1);
        Player savedPlayer2 = playerRepository.save(player2);

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

        assertEquals(true, playerBusy);
    }
}