package com.arenapointhub.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.MatchScoreUpdateDTO;
import com.arenapointhub.api.dto.PlayerSummaryDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final CategoryRepository categoryRepository;
    private final PlayerRepository playerRepository;

    public MatchService(MatchRepository matchRepository, CategoryRepository categoryRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.categoryRepository = categoryRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public MatchResponseDTO createMatch(MatchRequestDTO dto) {
        if (dto.getPlayer1Id().equals(dto.getPlayer2Id())) {
            throw new BusinessException("Um jogador não pode jogar contra ele mesmo.");
        }

        // Validação 1: Conflito de local/mesa e horário
        boolean courtOccupied = matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                dto.getTableOrCourt(), dto.getScheduledTime(), MatchStatus.CANCELED);
        if (courtOccupied) {
            throw new BusinessException("A mesa/quadra '" + dto.getTableOrCourt() + "' já está ocupada no horário " + dto.getScheduledTime());
        }

     // Validação 2: Conflito de agenda dos jogadores
        boolean playersBusy = matchRepository.existsByPlayerBusy(
                dto.getPlayer1Id(), dto.getPlayer2Id(), dto.getScheduledTime(), MatchStatus.CANCELED);

        if (playersBusy) {
            throw new BusinessException("Um dos jogadores já possui partida agendada no horário " + dto.getScheduledTime());
        }
        
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada: " + dto.getCategoryId()));

        Player player1 = playerRepository.findById(dto.getPlayer1Id())
                .orElseThrow(() -> new BusinessException("Jogador 1 não encontrado: " + dto.getPlayer1Id()));

        Player player2 = playerRepository.findById(dto.getPlayer2Id())
                .orElseThrow(() -> new BusinessException("Jogador 2 não encontrado: " + dto.getPlayer2Id()));

        Match match = new Match();
        match.setCategory(category);
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setTableOrCourt(dto.getTableOrCourt());
        match.setScheduledTime(dto.getScheduledTime());
        match.setStatus(dto.getStatus() != null ? dto.getStatus() : MatchStatus.SCHEDULED);

        Match savedMatch = matchRepository.save(match);
        return mapToResponseDTO(savedMatch);
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByCategory(Long categoryId) {
        return matchRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatchResponseDTO updateScore(Long matchId, MatchScoreUpdateDTO dto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("Partida não encontrada com ID: " + matchId));

        match.setScorePlayer1(dto.getScorePlayer1());
        match.setScorePlayer2(dto.getScorePlayer2());

        if (dto.getStatus() != null) {
            match.setStatus(dto.getStatus());
        }

        Match updatedMatch = matchRepository.save(match);
        return mapToResponseDTO(updatedMatch);
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByStatus(MatchStatus status) {
        return matchRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByPlayer(Long playerId) {
        return matchRepository.findByPlayer1IdOrPlayer2Id(playerId, playerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private MatchResponseDTO mapToResponseDTO(Match entity) {
        MatchResponseDTO dto = new MatchResponseDTO();
        dto.setId(entity.getId());
        dto.setCategoryId(entity.getCategory().getId());
        dto.setCategoryName(entity.getCategory().getName());

        dto.setPlayer1(new PlayerSummaryDTO(
                entity.getPlayer1().getId(),
                entity.getPlayer1().getName(),
                entity.getPlayer1().getClubAcademy()
        ));

        dto.setPlayer2(new PlayerSummaryDTO(
                entity.getPlayer2().getId(),
                entity.getPlayer2().getName(),
                entity.getPlayer2().getClubAcademy()
        ));

        dto.setTableOrCourt(entity.getTableOrCourt());
        dto.setScheduledTime(entity.getScheduledTime());
        dto.setScorePlayer1(entity.getScorePlayer1());
        dto.setScorePlayer2(entity.getScorePlayer2());
        dto.setStatus(entity.getStatus());
        return dto;
    }
    
    @Transactional
    public MatchResponseDTO updateMatch(Long id, MatchRequestDTO dto) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Partida não encontrada com ID: " + id));

        if (dto.getScorePlayer1() != null) {
            match.setScorePlayer1(dto.getScorePlayer1());
        }
        if (dto.getScorePlayer2() != null) {
            match.setScorePlayer2(dto.getScorePlayer2());
        }
        if (dto.getStatus() != null) {
            match.setStatus(dto.getStatus());
        }

        Match updatedMatch = matchRepository.save(match);
        return mapToResponseDTO(updatedMatch);
    }
}