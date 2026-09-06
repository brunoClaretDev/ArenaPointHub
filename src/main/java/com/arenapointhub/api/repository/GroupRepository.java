package com.arenapointhub.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.arenapointhub.api.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{
	List<Group> findByCategoryId(Long categoryId);

}
