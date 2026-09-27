package com.arenapointhub.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenapointhub.api.model.PlayerGlobalRanking;
import com.arenapointhub.api.repository.PlayerGlobalRankingRepository;

@ExtendWith(MockitoExtension.class)
class GlobalRankingControllerTest {

    @Mock
    private PlayerGlobalRankingRepository rankingRepository;

    private GlobalRankingController globalRankingController;

    @BeforeEach
    void setUp() {
        globalRankingController = new GlobalRankingController(rankingRepository);
    }

    @Test
    void shouldGetGlobalRankingByYear() {
        int year = 2026;
        List<PlayerGlobalRanking> expectedRanking =
                List.of(new PlayerGlobalRanking());

        when(rankingRepository.findByYearOrderByTotalPointsDesc(year))
                .thenReturn(expectedRanking);

        ResponseEntity<List<PlayerGlobalRanking>> response =
                globalRankingController.getGlobalRankingByYear(year);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedRanking, response.getBody());

        verify(rankingRepository).findByYearOrderByTotalPointsDesc(year);
    }

    @Test
    void shouldReturnEmptyListWhenThereIsNoRankingForYear() {
        int year = 2025;
        List<PlayerGlobalRanking> expectedRanking = List.of();

        when(rankingRepository.findByYearOrderByTotalPointsDesc(year))
                .thenReturn(expectedRanking);

        ResponseEntity<List<PlayerGlobalRanking>> response =
                globalRankingController.getGlobalRankingByYear(year);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedRanking, response.getBody());

        verify(rankingRepository).findByYearOrderByTotalPointsDesc(year);
    }
}