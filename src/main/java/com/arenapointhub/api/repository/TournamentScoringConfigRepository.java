package com.arenapointhub.api.repository;

import com.arenapointhub.api.model.TournamentScoringConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TournamentScoringConfigRepository extends JpaRepository<TournamentScoringConfig, Long> {
    Optional<TournamentScoringConfig> findByTournamentId(Long tournamentId);
}