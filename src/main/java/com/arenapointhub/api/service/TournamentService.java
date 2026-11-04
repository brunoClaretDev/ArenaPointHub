package com.arenapointhub.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Administrator;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.AdministratorRepository;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final AdministratorRepository administratorRepository;
    private final CategoryRepository categoryRepository;
    private final BracketService bracketService;

    public TournamentService(
            TournamentRepository tournamentRepository,
            AdministratorRepository administratorRepository,
            CategoryRepository categoryRepository,
            BracketService bracketService) {
        this.tournamentRepository = tournamentRepository;
        this.administratorRepository = administratorRepository;
        this.categoryRepository = categoryRepository;
        this.bracketService = bracketService;
    }

    @Transactional
    public TournamentResponseDTO createTournament(
            TournamentRequestDTO dto,
            Long administratorId) {

        Administrator administrator =
                administratorRepository.getReferenceById(administratorId);

        Tournament tournament = new Tournament();
        tournament.setAdministrator(administrator);

        copyDtoToEntity(dto, tournament);

        tournament.setStatus(dto.getStatus() == null
                ? TournamentStatus.UPCOMING
                : dto.getStatus());

        return mapToResponseDTO(tournamentRepository.save(tournament));
    }

    @Transactional(readOnly = true)
    public List<TournamentResponseDTO> getAllTournaments(Long administratorId) {
        return tournamentRepository
                .findAllByAdministrator_Id(administratorId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TournamentResponseDTO getTournamentById(
            Long id,
            Long administratorId) {

        Tournament tournament = findOwnedTournament(id, administratorId);
        return mapToResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO updateTournament(
            Long id,
            TournamentRequestDTO dto,
            Long administratorId) {

        Tournament tournament = findOwnedTournament(id, administratorId);

        copyDtoToEntity(dto, tournament);

        if (dto.getStatus() != null) {
            tournament.setStatus(dto.getStatus());
        }

        return mapToResponseDTO(tournamentRepository.save(tournament));
    }

    @Transactional
    public void deleteTournament(Long id, Long administratorId) {
        Tournament tournament = findOwnedTournament(id, administratorId);
        tournamentRepository.delete(tournament);
    }

    @Transactional
    public List<BracketResponseDTO> generatePlayoffs(
            Long tournamentId,
            Long administratorId) {

        findOwnedTournament(tournamentId, administratorId);

        List<Category> categories =
                categoryRepository.findByTournamentId(tournamentId);

        if (categories.isEmpty()) {
            throw new BusinessException(
                    "O torneio não possui categorias cadastradas.");
        }

        return categories.stream()
                .map(category ->
                        bracketService.generateKnockoutBracket(category.getId()))
                .toList();
    }

    private Tournament findOwnedTournament(
            Long tournamentId,
            Long administratorId) {

        return tournamentRepository
                .findByIdAndAdministrator_Id(tournamentId, administratorId)
                .orElseThrow(() -> new BusinessException(
                        "Torneio não encontrado com o ID: " + tournamentId));
    }

    private void copyDtoToEntity(
            TournamentRequestDTO dto,
            Tournament entity) {

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setSchedule(dto.getSchedule());
        entity.setLocation(dto.getLocation());
    }

    private TournamentResponseDTO mapToResponseDTO(Tournament entity) {
        TournamentResponseDTO dto = new TournamentResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setSchedule(entity.getSchedule());
        dto.setLocation(entity.getLocation());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}