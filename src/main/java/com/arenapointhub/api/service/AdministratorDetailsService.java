package com.arenapointhub.api.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.repository.AdministratorRepository;
import com.arenapointhub.api.security.AdministratorPrincipal;

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

        return new AdministratorPrincipal(
                administrator.getId(),
                administrator.getUsername(),
                administrator.getPasswordHash(),
                administrator.isActive());
    }
}