package com.arenapointhub.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arenapointhub.api.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByCategoryId(Long categoryId);

    List<Group> findByCategoryIdAndCategory_Tournament_Administrator_Id(
            Long categoryId, Long administratorId);

    Optional<Group> findByIdAndCategory_Tournament_Administrator_Id(
            Long groupId, Long administratorId);

    boolean existsByCategoryIdAndPlayersId(
            Long categoryId, Long playerId);
}