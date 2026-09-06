package com.arenapointhub.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.enums.MatchStatus;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    List<Match> findByCategoryId(Long categoryId);

    List<Match> findByStatus(MatchStatus status);

    List<Match> findByPlayer1IdOrPlayer2Id(Long player1Id, Long player2Id);
}