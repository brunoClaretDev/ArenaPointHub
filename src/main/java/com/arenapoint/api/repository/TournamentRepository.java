package com.arenapoint.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.arenapoint.api.model.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long>{

}
