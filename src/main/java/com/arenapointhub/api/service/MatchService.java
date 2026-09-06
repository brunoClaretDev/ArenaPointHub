package com.arenapointhub.api.service;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.PlayerSummaryDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Transactional
    public MatchResponseDTO createMatch(MatchRequestDTO dto) {
        if (dto.getPlayer1Id().equals(dto.getPlayer2Id())) {
            throw new BusinessException("Um jogador não pode jogar contra ele mesmo.");
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
}