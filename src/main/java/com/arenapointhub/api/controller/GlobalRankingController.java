package com.arenapointhub.api.controller;

import com.arenapointhub.api.model.PlayerGlobalRanking;
import com.arenapointhub.api.repository.PlayerGlobalRankingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class GlobalRankingController {

    private final PlayerGlobalRankingRepository rankingRepository;

    public GlobalRankingController(PlayerGlobalRankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    @GetMapping("/year/{year}/category/{categoryId}")
    public ResponseEntity<List<PlayerGlobalRanking>> getGlobalRankingByYearAndCategory(
            @PathVariable int year,
            @PathVariable Long categoryId) {

        List<PlayerGlobalRanking> ranking =
                rankingRepository.findByCategoryIdAndYearOrderByTotalPointsDesc(
                        categoryId, year);

        return ResponseEntity.ok(ranking);
    }
}