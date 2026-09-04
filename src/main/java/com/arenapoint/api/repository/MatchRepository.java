package com.arenapoint.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arenapoint.api.model.Match;

public interface MatchRepository extends JpaRepository<Match, Long>{
	List<Match> findByCategoryId(Long categoryId);
	List<Match> findByGroupId(Long groupId);

}
