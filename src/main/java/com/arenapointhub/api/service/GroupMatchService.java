package com.arenapointhub.api.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.repository.GroupRepository;

@Service
public class GroupMatchService {

    private final GroupRepository groupRepository;
    private final MatchService matchService;

    public GroupMatchService(GroupRepository groupRepository, MatchService matchService) {
        this.groupRepository = groupRepository;
        this.matchService = matchService;
    }

    @Transactional
    public List<MatchResponseDTO> generateRoundRobinMatchesForGroup(Long groupId, String tableOrCourt, String baseScheduledTime) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado com ID: " + groupId));

        List<Player> players = group.getPlayers();

        if (players == null || players.size() < 2) {
            throw new BusinessException("O grupo precisa ter pelo menos 2 jogadores para gerar as partidas.");
        }

        List<MatchResponseDTO> createdMatches = new ArrayList<>();
        
        // Formato padrão de hora ("HH:mm")
        LocalTime currentTime = (baseScheduledTime != null && !baseScheduledTime.isBlank()) 
                ? LocalTime.parse(baseScheduledTime, DateTimeFormatter.ofPattern("HH:mm")) 
                : LocalTime.of(14, 0);

        String court = (tableOrCourt != null && !tableOrCourt.isBlank()) ? tableOrCourt : "Mesa 1";

        // Algoritmo Round-Robin (Todos contra todos)
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                Player player1 = players.get(i);
                Player player2 = players.get(j);

                MatchRequestDTO matchDTO = new MatchRequestDTO();
                matchDTO.setCategoryId(group.getCategory().getId());
                matchDTO.setPlayer1Id(player1.getId());
                matchDTO.setPlayer2Id(player2.getId());
                matchDTO.setTableOrCourt(court);
                matchDTO.setScheduledTime(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));

                // Salva a partida usando o serviço existente (passando pelas validações)
                MatchResponseDTO response = matchService.createMatch(matchDTO);
                createdMatches.add(response);

                // Incrementa 30 minutos para a próxima partida não conflitar no mesmo horário/mesa
                currentTime = currentTime.plusMinutes(30);
            }
        }

        return createdMatches;
    }
}