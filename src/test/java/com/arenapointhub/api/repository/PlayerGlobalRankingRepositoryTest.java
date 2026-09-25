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

import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.PlayerGlobalRanking;

@DataJpaTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class PlayerGlobalRankingRepositoryTest {

    private final PlayerGlobalRankingRepository playerGlobalRankingRepository;
    private final PlayerRepository playerRepository;

    PlayerGlobalRankingRepositoryTest(
            PlayerGlobalRankingRepository playerGlobalRankingRepository,
            PlayerRepository playerRepository) {

        this.playerGlobalRankingRepository = playerGlobalRankingRepository;
        this.playerRepository = playerRepository;
    }

    @Test
    void shouldFindRankingByPlayerIdAndYear() {
        Player player = new Player();
        player.setName("Jogador Teste");
        player.setEmail("jogador.ranking@email.com");
        player.setBirthDate(LocalDate.of(2012, 5, 10));

        Player savedPlayer = playerRepository.save(player);

        PlayerGlobalRanking ranking = new PlayerGlobalRanking(
                savedPlayer,
                2026,
                150,
                5);

        playerGlobalRankingRepository.save(ranking);

        Optional<PlayerGlobalRanking> foundRanking =
                playerGlobalRankingRepository.findByPlayerIdAndYear(
                        savedPlayer.getId(),
                        2026);

        assertTrue(foundRanking.isPresent());
        assertEquals(savedPlayer.getId(), foundRanking.get().getPlayer().getId());
        assertEquals(2026, foundRanking.get().getYear());
        assertEquals(150, foundRanking.get().getTotalPoints());
        assertEquals(5, foundRanking.get().getTournamentsPlayed());
    }

    @Test
    void shouldFindRankingsByYearOrderedByTotalPointsDescending() {
        Player player1 = new Player();
        player1.setName("Jogador 1");
        player1.setEmail("jogador1.ranking@email.com");
        player1.setBirthDate(LocalDate.of(2012, 5, 10));

        Player player2 = new Player();
        player2.setName("Jogador 2");
        player2.setEmail("jogador2.ranking@email.com");
        player2.setBirthDate(LocalDate.of(2011, 6, 15));

        Player savedPlayer1 = playerRepository.save(player1);
        Player savedPlayer2 = playerRepository.save(player2);

        PlayerGlobalRanking ranking1 = new PlayerGlobalRanking(
                savedPlayer1,
                2026,
                100,
                4);

        PlayerGlobalRanking ranking2 = new PlayerGlobalRanking(
                savedPlayer2,
                2026,
                250,
                6);

        playerGlobalRankingRepository.save(ranking1);
        playerGlobalRankingRepository.save(ranking2);

        List<PlayerGlobalRanking> rankings =
                playerGlobalRankingRepository
                        .findByYearOrderByTotalPointsDesc(2026);

        assertEquals(2, rankings.size());
        assertEquals(250, rankings.get(0).getTotalPoints());
        assertEquals(100, rankings.get(1).getTotalPoints());
        assertEquals(
                savedPlayer2.getId(),
                rankings.get(0).getPlayer().getId());
        assertEquals(
                savedPlayer1.getId(),
                rankings.get(1).getPlayer().getId());
    }
}