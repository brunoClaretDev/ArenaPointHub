package com.arenapointhub.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arenapointhub.api.model.Administrator;

public interface AdministratorRepository
        extends JpaRepository<Administrator, Long> {

    Optional<Administrator> findByUsername(String username);

    boolean existsByUsername(String username);
}