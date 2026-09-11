package com.arenapointhub.api.repository;

import com.arenapointhub.api.model.PlayerGlobalRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerGlobalRankingRepository extends JpaRepository<PlayerGlobalRanking, Long> {
    Optional<PlayerGlobalRanking> findByPlayerIdAndYear(Long playerId, int year);
    List<PlayerGlobalRanking> findByYearOrderByTotalPointsDesc(int year);
}