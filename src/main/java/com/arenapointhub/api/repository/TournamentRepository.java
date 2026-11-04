package com.arenapointhub.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arenapointhub.api.model.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findAllByAdministrator_Id(Long administratorId);

    Optional<Tournament> findByIdAndAdministrator_Id(
            Long tournamentId,
            Long administratorId);

    boolean existsByIdAndAdministrator_Id(
            Long tournamentId,
            Long administratorId);
}