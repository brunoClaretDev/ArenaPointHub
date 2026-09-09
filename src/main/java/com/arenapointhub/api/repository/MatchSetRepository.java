package com.arenapointhub.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arenapointhub.api.model.MatchSet;

@Repository
public interface MatchSetRepository extends JpaRepository<MatchSet, Long> {
    
    List<MatchSet> findByMatchId(Long matchId);

    Optional<MatchSet> findByMatchIdAndSetNumber(Long matchId, Integer setNumber);
}