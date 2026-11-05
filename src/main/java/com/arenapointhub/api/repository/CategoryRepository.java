package com.arenapointhub.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arenapointhub.api.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByTournamentId(Long tournamentId);

    List<Category> findAllByTournament_Administrator_Id(Long administratorId);

    Optional<Category> findByIdAndTournament_Administrator_Id(
            Long categoryId,
            Long administratorId);

    boolean existsByIdAndTournament_Administrator_Id(
            Long categoryId,
            Long administratorId);
}
