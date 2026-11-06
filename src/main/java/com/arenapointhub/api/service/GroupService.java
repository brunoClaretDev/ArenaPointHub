
package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public GroupService(
            GroupRepository groupRepository,
            CategoryRepository categoryRepository,
            PlayerRepository playerRepository,
            MatchRepository matchRepository) {

        this.groupRepository = groupRepository;
        this.categoryRepository = categoryRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public GroupResponseDTO createGroup(
            GroupRequestDTO dto,
            Long administratorId) {

        Category category = findCategoryByAdministrator(
                dto.getCategoryId(), administratorId);

        Group group = new Group();
        group.setName(dto.getName());
        group.setCategory(category);

        if (dto.getPlayerIds() != null && !dto.getPlayerIds().isEmpty()) {
            List<Player> players = playerRepository.findAllById(dto.getPlayerIds());

            if (players.size() != dto.getPlayerIds().size()) {
                throw new BusinessException(
                        "Um ou mais jogadores informados não foram encontrados.");
            }

            for (Long playerId : dto.getPlayerIds()) {
                boolean alreadyInAnotherGroup =
                        groupRepository.existsByCategoryIdAndPlayersId(
                                dto.getCategoryId(), playerId);

                if (alreadyInAnotherGroup) {
                    throw new BusinessException(
                            "O jogador com ID " + playerId
                                    + " já pertence a um grupo desta categoria.");
                }
            }

            group.setPlayers(players);
        }

        Group savedGroup = groupRepository.save(group);

        return mapToResponseDTO(savedGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getGroupsByCategory(
            Long categoryId,
            Long administratorId) {

        findCategoryByAdministrator(categoryId, administratorId);

        return groupRepository
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        categoryId, administratorId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupById(
            Long id,
            Long administratorId) {

        Group group = findGroupByAdministrator(id, administratorId);

        return mapToResponseDTO(group);
    }

    @Transactional
    public GroupResponseDTO updateGroup(
            Long id,
            GroupRequestDTO dto,
            Long administratorId) {

        Group group = findGroupByAdministrator(id, administratorId);

        Category category = findCategoryByAdministrator(
                dto.getCategoryId(), administratorId);

        if (dto.getPlayerIds() != null && !dto.getPlayerIds().isEmpty()) {
            List<Player> players = playerRepository.findAllById(dto.getPlayerIds());

            if (players.size() != dto.getPlayerIds().size()) {
                throw new BusinessException(
                        "Um ou mais jogadores informados não foram encontrados.");
            }

            for (Long playerId : dto.getPlayerIds()) {
                boolean alreadyInAnotherGroup =
                        groupRepository.existsByCategoryIdAndPlayersId(
                                dto.getCategoryId(), playerId);

                boolean isCurrentPlayer = group.getPlayers().stream()
                        .anyMatch(player -> player.getId().equals(playerId));

                if (alreadyInAnotherGroup && !isCurrentPlayer) {
                    throw new BusinessException(
                            "O jogador com ID " + playerId
                                    + " já pertence a um grupo desta categoria.");
                }
            }

            group.setPlayers(players);
        } else {
            group.setPlayers(new ArrayList<>());
        }

        group.setName(dto.getName());
        group.setCategory(category);

        Group updatedGroup = groupRepository.save(group);

        return mapToResponseDTO(updatedGroup);
    }

    @Transactional
    public List<GroupResponseDTO> generateAndDistributeGroups(
            GroupGenerateRequestDTO dto,
            Long administratorId) {

        Category category = findCategoryByAdministrator(
                dto.getCategoryId(), administratorId);

        List<Group> existingGroups =
                groupRepository
                        .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                                dto.getCategoryId(), administratorId);

        if (!existingGroups.isEmpty()) {
            throw new BusinessException(
                    "Já existem grupos gerados para esta categoria.");
        }

        List<Player> players = new ArrayList<>(category.getPlayers());

        if (players.isEmpty()) {
            throw new BusinessException(
                    "Não há jogadores inscritos nesta categoria para gerar os grupos.");
        }

        Collections.shuffle(players);

        int totalPlayers = players.size();
        int targetSize = dto.getTargetGroupSize();

        if (targetSize <= 0) {
            throw new BusinessException(
                    "O tamanho alvo do grupo deve ser maior que zero.");
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
    public void generateMatchesForCategory(
            Long categoryId,
            Long administratorId) {

        Category category = findCategoryByAdministrator(
                categoryId, administratorId);

        List<Group> groups =
                groupRepository
                        .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                                categoryId, administratorId);

        if (groups.isEmpty()) {
            throw new BusinessException(
                    "Não há grupos gerados para esta categoria.");
        }

        for (Group group : groups) {
            List<Player> players = group.getPlayers();

            if (players == null || players.size() < 2) {
                continue;
            }

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
    public List<GroupStandingDTO> calculateGroupStandings(
            Long groupId,
            Long administratorId) {

        Group group = findGroupByAdministrator(groupId, administratorId);

        List<Player> players = group.getPlayers();
        List<Match> matches = matchRepository.findByGroupId(groupId);

        Map<Long, GroupStandingDTO> standingsMap = new HashMap<>();

        for (Player player : players) {
            GroupStandingDTO dto = new GroupStandingDTO();

            dto.setPlayerId(player.getId());
            dto.setPlayerName(player.getName());
            dto.setClubAcademy(player.getClubAcademy());

            standingsMap.put(player.getId(), dto);
        }

        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.FINISHED
                    && match.getStatus() != MatchStatus.WO) {
                continue;
            }

            Long player1Id = match.getPlayer1().getId();
            Long player2Id = match.getPlayer2().getId();

            GroupStandingDTO stats1 = standingsMap.get(player1Id);
            GroupStandingDTO stats2 = standingsMap.get(player2Id);

            if (stats1 != null && stats2 != null) {
                stats1.setMatchesPlayed(stats1.getMatchesPlayed() + 1);
                stats2.setMatchesPlayed(stats2.getMatchesPlayed() + 1);

                int setsPlayer1 = match.getScorePlayer1() != null
                        ? match.getScorePlayer1() : 0;

                int setsPlayer2 = match.getScorePlayer2() != null
                        ? match.getScorePlayer2() : 0;

                stats1.setSetsWon(stats1.getSetsWon() + setsPlayer1);
                stats1.setSetsLost(stats1.getSetsLost() + setsPlayer2);

                stats2.setSetsWon(stats2.getSetsWon() + setsPlayer2);
                stats2.setSetsLost(stats2.getSetsLost() + setsPlayer1);

                if (setsPlayer1 > setsPlayer2) {
                    stats1.setMatchesWon(stats1.getMatchesWon() + 1);
                    stats1.setPoints(stats1.getPoints() + 2);

                    stats2.setMatchesLost(stats2.getMatchesLost() + 1);
                    stats2.setPoints(stats2.getPoints() + 1);

                } else if (setsPlayer2 > setsPlayer1) {
                    stats2.setMatchesWon(stats2.getMatchesWon() + 1);
                    stats2.setPoints(stats2.getPoints() + 2);

                    stats1.setMatchesLost(stats1.getMatchesLost() + 1);
                    stats1.setPoints(stats1.getPoints() + 1);
                }
            }
        }

        List<GroupStandingDTO> standings =
                new ArrayList<>(standingsMap.values());

        for (GroupStandingDTO dto : standings) {
            dto.setSetDifference(dto.getSetsWon() - dto.getSetsLost());
        }

        standings.sort((a, b) -> {
            int pointsCompare = Integer.compare(
                    b.getPoints(), a.getPoints());

            if (pointsCompare != 0) {
                return pointsCompare;
            }

            int winsCompare = Integer.compare(
                    b.getMatchesWon(), a.getMatchesWon());

            if (winsCompare != 0) {
                return winsCompare;
            }

            int setDifferenceCompare = Integer.compare(
                    b.getSetDifference(), a.getSetDifference());

            if (setDifferenceCompare != 0) {
                return setDifferenceCompare;
            }

            return Integer.compare(
                    b.getSetsWon(), a.getSetsWon());
        });

        return standings;
    }

    @Transactional
    public void removePlayerFromGroup(
            Long groupId,
            Long playerId,
            Long administratorId) {

        Group group = findGroupByAdministrator(groupId, administratorId);

        List<Match> existingMatches =
                matchRepository.findByGroupId(groupId);

        boolean hasPlayedMatches = existingMatches.stream()
                .anyMatch(match ->
                        match.getStatus() == MatchStatus.IN_PROGRESS
                                || match.getStatus() == MatchStatus.FINISHED
                                || match.getStatus() == MatchStatus.WO);

        if (hasPlayedMatches) {
            throw new BusinessException(
                    "Não é possível remover jogadores de um grupo que já possui partidas iniciadas ou realizadas.");
        }

        boolean removed = group.getPlayers()
                .removeIf(player -> player.getId().equals(playerId));

        if (!removed) {
            throw new BusinessException(
                    "O jogador informado não pertence a este grupo.");
        }

        for (Match match : existingMatches) {
            matchRepository.delete(match);
        }

        groupRepository.save(group);
    }

    private GroupResponseDTO mapToResponseDTO(Group entity) {
        GroupResponseDTO dto = new GroupResponseDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCategoryId(entity.getCategory().getId());
        dto.setCategoryName(entity.getCategory().getName());

        List<PlayerSummaryDTO> playerDTOs = entity.getPlayers() != null
                ? entity.getPlayers().stream()
                        .map(player -> new PlayerSummaryDTO(
                                player.getId(),
                                player.getName(),
                                player.getClubAcademy()))
                        .collect(Collectors.toList())
                : new ArrayList<>();

        dto.setPlayers(playerDTOs);

        return dto;
    }

    private Category findCategoryByAdministrator(
            Long categoryId,
            Long administratorId) {

        return categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        categoryId, administratorId)
                .orElseThrow(() -> new BusinessException(
                        "Categoria não encontrada com ID: " + categoryId));
    }

    private Group findGroupByAdministrator(
            Long groupId,
            Long administratorId) {

        return groupRepository
                .findByIdAndCategory_Tournament_Administrator_Id(
                        groupId, administratorId)
                .orElseThrow(() -> new BusinessException(
                        "Grupo não encontrado com ID: " + groupId));
    }
}
