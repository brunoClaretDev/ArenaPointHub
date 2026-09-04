package com.arenapoint.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arenapoint.api.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{
	List<Group> findByCategoryId(Long categoryId);

}
