package com.arenapointhub.api.repository;

import com.arenapointhub.api.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsByEmail(String email);
}