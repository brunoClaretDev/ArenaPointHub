package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.PlayerGlobalRanking;
import com.arenapointhub.api.model.Tournament;

@DataJpaTest(properties = {
	    "spring.jpa.hibernate.ddl-auto=create-drop"
	})
@TestConstructor(autowireMode = AutowireMode.ALL)
class PlayerGlobalRankingRepositoryTest {

    private final PlayerGlobalRankingRepository playerGlobalRankingRepository;
    private final PlayerRepository playerRepository;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;

    PlayerGlobalRankingRepositoryTest(
            PlayerGlobalRankingRepository playerGlobalRankingRepository,
            PlayerRepository playerRepository,
            CategoryRepository categoryRepository,
            TournamentRepository tournamentRepository) {

        this.playerGlobalRankingRepository = playerGlobalRankingRepository;
        this.playerRepository = playerRepository;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Test
    void shouldFindRankingByPlayerIdCategoryIdAndYear() {
        Player savedPlayer = createAndSavePlayer(
                "Jogador Teste",
                "jogador.ranking@email.com"
        );

        Category savedCategory = createAndSaveCategory(
                "Categoria Teste"
        );

        PlayerGlobalRanking ranking = new PlayerGlobalRanking(
                savedPlayer,
                savedCategory,
                2026,
                150,
                5
        );

        playerGlobalRankingRepository.save(ranking);

        Optional<PlayerGlobalRanking> foundRanking =
                playerGlobalRankingRepository
                        .findByPlayerIdAndCategoryIdAndYear(
                                savedPlayer.getId(),
                                savedCategory.getId(),
                                2026
                        );

        assertTrue(foundRanking.isPresent());
        assertEquals(
                savedPlayer.getId(),
                foundRanking.get().getPlayer().getId()
        );
        assertEquals(
                savedCategory.getId(),
                foundRanking.get().getCategory().getId()
        );
        assertEquals(2026, foundRanking.get().getYear());
        assertEquals(150, foundRanking.get().getTotalPoints());
        assertEquals(5, foundRanking.get().getTournamentsPlayed());
    }

    @Test
    void shouldFindRankingsByCategoryAndYearOrderedByTotalPointsDescending() {
        Player savedPlayer1 = createAndSavePlayer(
                "Jogador 1",
                "jogador1.ranking@email.com"
        );

        Player savedPlayer2 = createAndSavePlayer(
                "Jogador 2",
                "jogador2.ranking@email.com"
        );

        Category savedCategory = createAndSaveCategory(
                "Categoria Teste"
        );

        PlayerGlobalRanking ranking1 = new PlayerGlobalRanking(
                savedPlayer1,
                savedCategory,
                2026,
                100,
                4
        );

        PlayerGlobalRanking ranking2 = new PlayerGlobalRanking(
                savedPlayer2,
                savedCategory,
                2026,
                250,
                6
        );

        playerGlobalRankingRepository.save(ranking1);
        playerGlobalRankingRepository.save(ranking2);

        List<PlayerGlobalRanking> rankings =
                playerGlobalRankingRepository
                        .findByCategoryIdAndYearOrderByTotalPointsDesc(
                                savedCategory.getId(),
                                2026
                        );

        assertEquals(2, rankings.size());
        assertEquals(250, rankings.get(0).getTotalPoints());
        assertEquals(100, rankings.get(1).getTotalPoints());

        assertEquals(
                savedPlayer2.getId(),
                rankings.get(0).getPlayer().getId()
        );
        assertEquals(
                savedPlayer1.getId(),
                rankings.get(1).getPlayer().getId()
        );
    }

    private Player createAndSavePlayer(String name, String email) {
        Player player = new Player();
        player.setName(name);
        player.setEmail(email);
        player.setBirthDate(LocalDate.of(2012, 5, 10));

        return playerRepository.save(player);
    }

    private Category createAndSaveCategory(String name) {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio de Teste");

        Tournament savedTournament = tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName(name);
        category.setTournament(savedTournament);

        return categoryRepository.save(category);
    }
}