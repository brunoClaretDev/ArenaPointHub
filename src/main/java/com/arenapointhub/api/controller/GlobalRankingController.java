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

    @GetMapping("/year/{year}")
    public ResponseEntity<List<PlayerGlobalRanking>> getGlobalRankingByYear(@PathVariable int year) {
        List<PlayerGlobalRanking> ranking = rankingRepository.findByYearOrderByTotalPointsDesc(year);
        return ResponseEntity.ok(ranking);
    }
}