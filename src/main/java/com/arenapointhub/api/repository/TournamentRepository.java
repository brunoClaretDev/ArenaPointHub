package com.arenapointhub.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arenapointhub.api.model.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long>{

}
