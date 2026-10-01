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

    private static final int TEST_YEAR = 2026;
    private static final Long TEST_CATEGORY_ID = 1L;

    @Mock
    private PlayerGlobalRankingRepository rankingRepository;

    private GlobalRankingController globalRankingController;

    @BeforeEach
    void setUp() {
        globalRankingController =
                new GlobalRankingController(rankingRepository);
    }

    @Test
    void shouldReturnGlobalRankingForYearAndCategory() {
        List<PlayerGlobalRanking> expectedRanking =
                List.of(new PlayerGlobalRanking());

        when(rankingRepository
                .findByCategoryIdAndYearOrderByTotalPointsDesc(
                        TEST_CATEGORY_ID, TEST_YEAR))
                .thenReturn(expectedRanking);

        ResponseEntity<List<PlayerGlobalRanking>> response =
                globalRankingController.getGlobalRankingByYearAndCategory(
                        TEST_YEAR, TEST_CATEGORY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedRanking, response.getBody());

        verify(rankingRepository)
                .findByCategoryIdAndYearOrderByTotalPointsDesc(
                        TEST_CATEGORY_ID, TEST_YEAR);
    }

    @Test
    void shouldReturnEmptyListWhenNoRankingExistsForYearAndCategory() {
        List<PlayerGlobalRanking> expectedRanking = List.of();

        when(rankingRepository
                .findByCategoryIdAndYearOrderByTotalPointsDesc(
                        TEST_CATEGORY_ID, TEST_YEAR))
                .thenReturn(expectedRanking);

        ResponseEntity<List<PlayerGlobalRanking>> response =
                globalRankingController.getGlobalRankingByYearAndCategory(
                        TEST_YEAR, TEST_CATEGORY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedRanking, response.getBody());

        verify(rankingRepository)
                .findByCategoryIdAndYearOrderByTotalPointsDesc(
                        TEST_CATEGORY_ID, TEST_YEAR);
    }
}