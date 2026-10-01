package com.arenapointhub.api.repository;

import com.arenapointhub.api.model.PlayerGlobalRanking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerGlobalRankingRepository
        extends JpaRepository<PlayerGlobalRanking, Long> {

    Optional<PlayerGlobalRanking> findByPlayerIdAndCategoryIdAndYear(
            Long playerId,
            Long categoryId,
            int year);

    List<PlayerGlobalRanking> findByCategoryIdAndYearOrderByTotalPointsDesc(
            Long categoryId,
            int year);
}