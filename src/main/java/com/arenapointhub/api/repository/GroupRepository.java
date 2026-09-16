package com.arenapointhub.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.arenapointhub.api.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndPlayersId(Long categoryId, Long playerId);
}