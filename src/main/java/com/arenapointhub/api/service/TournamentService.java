
package com.arenapointhub.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final BracketService bracketService;

    public TournamentService(
            TournamentRepository tournamentRepository,
            CategoryRepository categoryRepository,
            BracketService bracketService) {
        this.tournamentRepository = tournamentRepository;
        this.categoryRepository = categoryRepository;
        this.bracketService = bracketService;
    }

    @Transactional
    public TournamentResponseDTO createTournament(TournamentRequestDTO dto) {
        Tournament tournament = new Tournament();
        copyDtoToEntity(dto, tournament);

        if (dto.getStatus() == null) {
            tournament.setStatus(TournamentStatus.UPCOMING);
        } else {
            tournament.setStatus(dto.getStatus());
        }

        Tournament savedTournament = tournamentRepository.save(tournament);
        return mapToResponseDTO(savedTournament);
    }

    @Transactional(readOnly = true)
    public List<TournamentResponseDTO> getAllTournaments() {
        return tournamentRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TournamentResponseDTO getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Torneio não encontrado com o ID: " + id));
        return mapToResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO updateTournament(
            Long id,
            TournamentRequestDTO dto) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Torneio não encontrado com o ID: " + id));

        copyDtoToEntity(dto, tournament);

        if (dto.getStatus() != null) {
            tournament.setStatus(dto.getStatus());
        }

        Tournament updatedTournament = tournamentRepository.save(tournament);
        return mapToResponseDTO(updatedTournament);
    }

    @Transactional
    public void deleteTournament(Long id) {
        if (!tournamentRepository.existsById(id)) {
            throw new BusinessException(
                    "Torneio não encontrado com o ID: " + id);
        }

        tournamentRepository.deleteById(id);
    }

    @Transactional
    public List<BracketResponseDTO> generatePlayoffs(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new BusinessException(
                    "Torneio não encontrado com o ID: " + tournamentId);
        }

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
