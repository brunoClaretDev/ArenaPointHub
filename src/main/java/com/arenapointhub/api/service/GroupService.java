package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.GroupGenerateRequestDTO;
import com.arenapointhub.api.dto.GroupRequestDTO;
import com.arenapointhub.api.dto.GroupResponseDTO;
import com.arenapointhub.api.dto.GroupStandingDTO;
import com.arenapointhub.api.dto.PlayerSummaryDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final CategoryRepository categoryRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    public GroupService(GroupRepository groupRepository, 
                        CategoryRepository categoryRepository, 
                        PlayerRepository playerRepository, 
                        MatchRepository matchRepository) {
        this.groupRepository = groupRepository;
        this.categoryRepository = categoryRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public GroupResponseDTO createGroup(GroupRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com ID: " + dto.getCategoryId()));

        Group group = new Group();
        group.setName(dto.getName());
        group.setCategory(category);

        if (dto.getPlayerIds() != null && !dto.getPlayerIds().isEmpty()) {
            List<Player> players = playerRepository.findAllById(dto.getPlayerIds());
            if (players.size() != dto.getPlayerIds().size()) {
                throw new BusinessException("Um ou mais jogadores informados não foram encontrados.");
            }
            group.setPlayers(players);
        }

        Group savedGroup = groupRepository.save(group);
        return mapToResponseDTO(savedGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getGroupsByCategory(Long categoryId) {
        return groupRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado com ID: " + id));
        return mapToResponseDTO(group);
    }

    private GroupResponseDTO mapToResponseDTO(Group entity) {
        GroupResponseDTO dto = new GroupResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCategoryId(entity.getCategory().getId());
        dto.setCategoryName(entity.getCategory().getName());

        List<PlayerSummaryDTO> playerDTOs = entity.getPlayers() != null ?
                entity.getPlayers().stream()
                        .map(p -> new PlayerSummaryDTO(p.getId(), p.getName(), p.getClubAcademy()))
                        .collect(Collectors.toList()) : new ArrayList<>();

        dto.setPlayers(playerDTOs);
        return dto;
    }
    
    @Transactional
    public List<GroupResponseDTO> generateAndDistributeGroups(GroupGenerateRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com ID: " + dto.getCategoryId()));

        List<Player> players = playerRepository.findAll(); 
        
        if (players.isEmpty()) {
            throw new BusinessException("Não há jogadores cadastrados para gerar os grupos.");
        }

        java.util.Collections.shuffle(players);

        int totalPlayers = players.size();
        int targetSize = dto.getTargetGroupSize();

        if (targetSize <= 0) {
            throw new BusinessException("O tamanho alvo do grupo deve ser maior que zero.");
        }

        int numberOfGroups = totalPlayers / targetSize;
        if (numberOfGroups == 0) {
            numberOfGroups = 1; 
        }

        List<List<Player>> distributedLists = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            distributedLists.add(new ArrayList<>());
        }

        int groupIndex = 0;
        for (Player player : players) {
            distributedLists.get(groupIndex).add(player);
            groupIndex = (groupIndex + 1) % numberOfGroups;
        }

        List<GroupResponseDTO> createdGroups = new ArrayList<>();

        for (int i = 0; i < distributedLists.size(); i++) {
            List<Player> groupPlayers = distributedLists.get(i);
            
            Group group = new Group();
            char groupLetter = (char) ('A' + i);
            group.setName("Grupo " + groupLetter);
            group.setCategory(category);
            group.setPlayers(groupPlayers);

            Group savedGroup = groupRepository.save(group);
            createdGroups.add(mapToResponseDTO(savedGroup));
        }

        return createdGroups;
    }
    
    @Transactional
    public void generateMatchesForCategory(Long categoryId) {
        List<Group> groups = groupRepository.findByCategoryId(categoryId);
        
        if (groups.isEmpty()) {
            throw new BusinessException("Não há grupos gerados para esta categoria.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));

        for (Group group : groups) {
            List<Player> players = group.getPlayers();
            
            if (players == null || players.size() < 2) {
                continue; 
            }

            // Algoritmo Round-Robin (Todos contra todos em turno único)
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) {
                    Player player1 = players.get(i);
                    Player player2 = players.get(j);

                    Match match = new Match();
                    match.setCategory(category);
                    match.setGroup(group);
                    match.setPlayer1(player1);
                    match.setPlayer2(player2);
                    match.setStatus(MatchStatus.SCHEDULED);

                    matchRepository.save(match);
                }
            }
        }
    }
    @Transactional(readOnly = true)
    public List<GroupStandingDTO> calculateGroupStandings(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado com ID: " + groupId));

        List<Player> players = group.getPlayers();
        List<Match> matches = matchRepository.findByGroupId(groupId); // Garanta que possui esse método no MatchRepository

        // Mapa para acumular as estatísticas de cada jogador
        java.util.Map<Long, GroupStandingDTO> standingsMap = new java.util.HashMap();

        for (Player player : players) {
            GroupStandingDTO dto = new GroupStandingDTO();
            dto.setPlayerId(player.getId());
            dto.setPlayerName(player.getName());
            dto.setClubAcademy(player.getClubAcademy());
            standingsMap.put(player.getId(), dto);
        }

        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.FINISHED) {
                continue; // Processa apenas partidas finalizadas
            }

            Long p1Id = match.getPlayer1().getId();
            Long p2Id = match.getPlayer2().getId();

            GroupStandingDTO stats1 = standingsMap.get(p1Id);
            GroupStandingDTO stats2 = standingsMap.get(p2Id);

            if (stats1 != null && stats2 != null) {
                stats1.setMatchesPlayed(stats1.getMatchesPlayed() + 1);
                stats2.setMatchesPlayed(stats2.getMatchesPlayed() + 1);

                int setsP1 = match.getScorePlayer1() != null ? match.getScorePlayer1() : 0;
                int setsP2 = match.getScorePlayer2() != null ? match.getScorePlayer2() : 0;

                stats1.setSetsWon(stats1.getSetsWon() + setsP1);
                stats1.setSetsLost(stats1.getSetsLost() + setsP2);
                
                stats2.setSetsWon(stats2.getSetsWon() + setsP2);
                stats2.setSetsLost(stats2.getSetsLost() + setsP1);

                if (setsP1 > setsP2) {
                    stats1.setMatchesWon(stats1.getMatchesWon() + 1);
                    stats1.setPoints(stats1.getPoints() + 2); // Ex: 2 pontos por vitória
                    stats2.setMatchesLost(stats2.getMatchesLost() + 1);
                    stats2.setPoints(stats2.getPoints() + 1); // Ex: 1 ponto por derrota
                } else if (setsP2 > setsP1) {
                    stats2.setMatchesWon(stats2.getMatchesWon() + 1);
                    stats2.setPoints(stats2.getPoints() + 2);
                    stats1.setMatchesLost(stats1.getMatchesLost() + 1);
                    stats1.setPoints(stats1.getPoints() + 1);
                }
            }
        }

        // Calcula o saldo de sets e converte para lista
        List<GroupStandingDTO> standings = new ArrayList<>(standingsMap.values());
        for (GroupStandingDTO dto : standings) {
            dto.setSetDifference(dto.getSetsWon() - dto.getSetsLost());
        }

        // Ordena por Pontos (desc), depois por Saldo de Sets (desc)
        standings.sort((a, b) -> {
            int pointsCompare = Integer.compare(b.getPoints(), a.getPoints());
            if (pointsCompare != 0) return pointsCompare;
            return Integer.compare(b.getSetDifference(), a.getSetDifference());
        });

        return standings;
    }
}