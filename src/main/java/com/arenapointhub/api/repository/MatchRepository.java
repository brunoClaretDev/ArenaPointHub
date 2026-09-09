package com.arenapointhub.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.enums.MatchStatus;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    List<Match> findByCategoryId(Long categoryId);

    List<Match> findByStatus(MatchStatus status);

    List<Match> findByPlayer1IdOrPlayer2Id(Long player1Id, Long player2Id);
    
    List<Match> findByGroupId(Long groupId);

    // Valida se a mesa/quadra já está ocupada no mesmo horário
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.tableOrCourt = :tableOrCourt AND m.scheduledTime = :scheduledTime AND m.status <> :status")
    boolean existsByTableOrCourtAndScheduledTimeAndStatusNot(
            @Param("tableOrCourt") String tableOrCourt, 
            @Param("scheduledTime") String scheduledTime, 
            @Param("status") MatchStatus status);

    // Valida se o jogador 1 ou jogador 2 já tem partida no mesmo horário
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE (m.player1.id = :p1Id OR m.player2.id = :p1Id OR m.player1.id = :p2Id OR m.player2.id = :p2Id) AND m.scheduledTime = :scheduledTime AND m.status <> :status")
    boolean existsByPlayerBusy(
            @Param("p1Id") Long player1Id, 
            @Param("p2Id") Long player2Id, 
            @Param("scheduledTime") String scheduledTime, 
            @Param("status") MatchStatus status);
}