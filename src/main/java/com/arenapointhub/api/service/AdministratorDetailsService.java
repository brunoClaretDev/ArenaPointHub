package com.arenapointhub.api.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.repository.AdministratorRepository;

@Service
public class AdministratorDetailsService implements UserDetailsService {

    private final AdministratorRepository administratorRepository;

    public AdministratorDetailsService(
            AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        Administrator administrator = administratorRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Administrador não encontrado."));

        return User.withUsername(administrator.getUsername())
                .password(administrator.getPasswordHash())
                .roles("ADMIN")
                .disabled(!administrator.isActive())
                .build();
    }
}