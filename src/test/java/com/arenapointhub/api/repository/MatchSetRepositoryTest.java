package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.MatchSet;
import com.arenapointhub.api.model.Tournament;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MatchSetRepositoryTest {

    private final MatchSetRepository matchSetRepository;
    private final MatchRepository matchRepository;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;

    MatchSetRepositoryTest(
            MatchSetRepository matchSetRepository,
            MatchRepository matchRepository,
            CategoryRepository categoryRepository,
            TournamentRepository tournamentRepository) {

        this.matchSetRepository = matchSetRepository;
        this.matchRepository = matchRepository;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Test
    void shouldFindMatchSetsByMatchId() {
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

        Match savedMatch =
                matchRepository.save(match);

        MatchSet set1 = new MatchSet(
                savedMatch,
                1,
                11,
                8);

        MatchSet set2 = new MatchSet(
                savedMatch,
                2,
                11,
                9);

        matchSetRepository.save(set1);
        matchSetRepository.save(set2);

        List<MatchSet> matchSets =
                matchSetRepository.findByMatchId(
                        savedMatch.getId());

        assertEquals(2, matchSets.size());
        assertEquals(
                savedMatch.getId(),
                matchSets.get(0).getMatch().getId());
        assertEquals(
                savedMatch.getId(),
                matchSets.get(1).getMatch().getId());
    }
    
    @Test
    void shouldFindMatchSetByMatchIdAndSetNumber() {
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

        Match savedMatch =
                matchRepository.save(match);

        MatchSet set1 = new MatchSet(
                savedMatch,
                1,
                11,
                8);

        MatchSet set2 = new MatchSet(
                savedMatch,
                2,
                11,
                9);

        matchSetRepository.save(set1);
        matchSetRepository.save(set2);

        Optional<MatchSet> foundSet =
                matchSetRepository.findByMatchIdAndSetNumber(
                        savedMatch.getId(),
                        2);

        assertTrue(foundSet.isPresent());
        assertEquals(2, foundSet.get().getSetNumber());
        assertEquals(11, foundSet.get().getScorePlayer1());
        assertEquals(9, foundSet.get().getScorePlayer2());
    }
    
}