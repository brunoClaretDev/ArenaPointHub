package com.arenapointhub.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.arenapointhub.api.model.Match;

public interface MatchRepository extends JpaRepository<Match, Long>{
	List<Match> findByCategoryId(Long categoryId);
	List<Match> findByGroupId(Long groupId);

}
