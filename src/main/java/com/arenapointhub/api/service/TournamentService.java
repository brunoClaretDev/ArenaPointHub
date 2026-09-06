package com.arenapointhub.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.TournamentRepository;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

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
                .orElseThrow(() -> new BusinessException("Torneio não encontrado com o ID: " + id));
        return mapToResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO updateTournament(Long id, TournamentRequestDTO dto) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Torneio não encontrado com o ID: " + id));

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
            throw new BusinessException("Torneio não encontrado com o ID: " + id);
        }
        tournamentRepository.deleteById(id);
    }

    private void copyDtoToEntity(TournamentRequestDTO dto, Tournament entity) {
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