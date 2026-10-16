package com.arenapointhub.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.repository.AdministratorRepository;

@Service
public class AdministratorService {

    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministratorService(
            AdministratorRepository administratorRepository,
            PasswordEncoder passwordEncoder) {
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Administrator createAdministrator(
            String username,
            String rawPassword) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "O login do administrador é obrigatório.");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "A senha do administrador é obrigatória.");
        }

        String normalizedUsername = username.trim();

        if (administratorRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException(
                    "Já existe um administrador com esse login.");
        }

        Administrator administrator = new Administrator();
        administrator.setUsername(normalizedUsername);
        administrator.setPasswordHash(
                passwordEncoder.encode(rawPassword));
        administrator.setActive(true);

        return administratorRepository.save(administrator);
    }
}