package com.arenapointhub.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.arenapointhub.api.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByTournamentId(Long tournamentId);

}
