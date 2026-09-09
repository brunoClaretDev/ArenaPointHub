package com.arenapointhub.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class StandingDTOTest {

    @Test
    void shouldSortStandingsByPointsFirst() {
        StandingDTO playerA = new StandingDTO(1L, "Player A", "Club A", 3, 1, 2, 3, 4, 4); // 4 pontos
        StandingDTO playerB = new StandingDTO(2L, "Player B", "Club B", 3, 2, 1, 5, 3, 5); // 5 pontos

        List<StandingDTO> list = new ArrayList<>();
        list.add(playerA);
        list.add(playerB);

        Collections.sort(list);

        assertEquals("Player B", list.get(0).getPlayerName());
        assertEquals("Player A", list.get(1).getPlayerName());
    }

    @Test
    void shouldTieBreakByWinsWhenPointsAreEqual() {
        // Ambos com 4 pontos, mas Player B tem mais vitórias (2 contra 1)
        StandingDTO playerA = new StandingDTO(1L, "Player A", "Club A", 3, 1, 2, 4, 3, 4); 
        StandingDTO playerB = new StandingDTO(2L, "Player B", "Club B", 3, 2, 1, 4, 4, 4); 

        List<StandingDTO> list = new ArrayList<>();
        list.add(playerA);
        list.add(playerB);

        Collections.sort(list);

        assertEquals("Player B", list.get(0).getPlayerName());
        assertEquals("Player A", list.get(1).getPlayerName());
    }

    @Test
    void shouldTieBreakBySetBalanceWhenPointsAndWinsAreEqual() {
        // Ambos com 4 pontos e 1 vitória, mas Player A tem melhor saldo de sets (+2 vs 0)
        StandingDTO playerA = new StandingDTO(1L, "Player A", "Club A", 2, 1, 1, 5, 3, 4); // Saldo +2
        StandingDTO playerB = new StandingDTO(2L, "Player B", "Club B", 2, 1, 1, 4, 4, 4); // Saldo 0

        List<StandingDTO> list = new ArrayList<>();
        list.add(playerB);
        list.add(playerA);

        Collections.sort(list);

        assertEquals("Player A", list.get(0).getPlayerName());
        assertEquals("Player B", list.get(1).getPlayerName());
    }

    @Test
    void shouldTieBreakBySetsWonWhenPointsWinsAndBalanceAreAreEqual() {
        // Mesmo pontos (4), mesmas vitórias (1), mesmo saldo (+1), mas Player B tem mais sets ganhos (6 contra 5)
        StandingDTO playerA = new StandingDTO(1L, "Player A", "Club A", 2, 1, 1, 5, 4, 4); // Saldo +1, SetsWon 5
        StandingDTO playerB = new StandingDTO(2L, "Player B", "Club B", 2, 1, 1, 6, 5, 4); // Saldo +1, SetsWon 6

        List<StandingDTO> list = new ArrayList<>();
        list.add(playerA);
        list.add(playerB);

        Collections.sort(list);

        assertEquals("Player B", list.get(0).getPlayerName());
        assertEquals("Player A", list.get(1).getPlayerName());
    }
}