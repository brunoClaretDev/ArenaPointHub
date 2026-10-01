package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.dto.GroupStandingDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.PlayerSummaryDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;

import jakarta.transaction.Transactional;

@Service
public class BracketService {

    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final CategoryRepository categoryRepository;

    public BracketService(
            GroupRepository groupRepository,
            GroupService groupService,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            CategoryRepository categoryRepository) {

        this.groupRepository = groupRepository;
        this.groupService = groupService;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public BracketResponseDTO generateKnockoutBracket(
            Long categoryId,
            int targetBracketSize) {

        Category category = findCategory(categoryId);
        validateBracketSize(targetBracketSize);

        List<Player> qualifiedPlayers =
                getOrderedQualifiedPlayers(categoryId);

        int totalQualifiedPlayers = qualifiedPlayers.size();

        List<Player> playersWithBye = determinePlayersWithBye(
                categoryId,
                totalQualifiedPlayers,
                targetBracketSize
        );

        List<Match> createdMatches = new ArrayList<>();

        createdMatches.addAll(
                createFirstRoundKnockoutMatches(
                        category,
                        qualifiedPlayers,
                        playersWithBye,
                        targetBracketSize
                )
        );

        createdMatches.addAll(
                createSubsequentRoundMatches(
                        category,
                        targetBracketSize
                )
        );

        advancePlayersWithBye(createdMatches, targetBracketSize);

        BracketResponseDTO response = new BracketResponseDTO();
        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setTargetBracketSize(targetBracketSize);
        response.setMatches(
                createdMatches.stream()
                        .map(this::mapMatchToResponseDTO)
                        .toList()
        );

        return response;
    }

    private void advancePlayersWithBye(
            List<Match> createdMatches,
            int bracketSize) {

        MatchPhase firstRoundPhase =
                determineFirstRoundPhase(bracketSize);

        MatchPhase nextPhase = getNextPhase(firstRoundPhase);

        if (nextPhase == null) {
            return;
        }

        List<Match> firstRoundMatches = createdMatches.stream()
                .filter(match -> match.getPhase() == firstRoundPhase)
                .sorted(Comparator.comparing(Match::getId))
                .toList();

        List<Match> nextRoundMatches = createdMatches.stream()
                .filter(match -> match.getPhase() == nextPhase)
                .sorted(Comparator.comparing(Match::getId))
                .toList();

        for (int i = 0; i < firstRoundMatches.size(); i++) {
            Match match = firstRoundMatches.get(i);

            if (match.getStatus() != MatchStatus.FINISHED) {
                continue;
            }

            Player winner = getByeWinner(match);

            if (winner == null) {
                continue;
            }

            int targetMatchIndex = i / 2;

            if (targetMatchIndex >= nextRoundMatches.size()) {
                continue;
            }

            Match targetMatch = nextRoundMatches.get(targetMatchIndex);

            if (i % 2 == 0) {
                targetMatch.setPlayer1(winner);
            } else {
                targetMatch.setPlayer2(winner);
            }

            matchRepository.save(targetMatch);
        }
    }

    private Player getByeWinner(Match match) {

        if (match.getPlayer1() != null && match.getPlayer2() == null) {
            return match.getPlayer1();
        }

        if (match.getPlayer2() != null && match.getPlayer1() == null) {
            return match.getPlayer2();
        }

        return null;
    }

    public List<Player> getOrderedGroupWinners(Long categoryId) {

        List<Group> groups = getOrderedGroups(categoryId);
        List<Player> winners = new ArrayList<>();

        for (Group group : groups) {
            List<GroupStandingDTO> standings =
                    groupService.calculateGroupStandings(group.getId());

            if (!standings.isEmpty()) {
                Long playerId = standings.get(0).getPlayerId();

                playerRepository.findById(playerId)
                        .ifPresent(winners::add);
            }
        }

        return winners;
    }
    
    private List<Player> getOrderedSecondPlacePlayers(Long categoryId) {

        List<Group> groups = getOrderedGroups(categoryId);

        List<GroupStandingDTO> secondPlaceStandings = new ArrayList<>();

        for (Group group : groups) {

            List<GroupStandingDTO> standings =
                    groupService.calculateGroupStandings(group.getId());

            if (standings.size() > 1) {
                secondPlaceStandings.add(standings.get(1));
            }
        }

        secondPlaceStandings.sort(
                Comparator.comparingInt(GroupStandingDTO::getPoints)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingInt(
                                        GroupStandingDTO::getMatchesWon
                                ).reversed()
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        GroupStandingDTO::getSetDifference
                                ).reversed()
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        GroupStandingDTO::getSetsWon
                                ).reversed()
                        )
        );

        List<Player> secondPlacePlayers = new ArrayList<>();

        for (GroupStandingDTO standing : secondPlaceStandings) {
            playerRepository.findById(standing.getPlayerId())
                    .ifPresent(secondPlacePlayers::add);
        }

        return secondPlacePlayers;
    }

    public List<Player> getOrderedQualifiedPlayers(Long categoryId) {

        List<Group> groups = getOrderedGroups(categoryId);

        List<Player> firstPlacePlayers = new ArrayList<>();
        List<Player> secondPlacePlayers = new ArrayList<>();

        for (Group group : groups) {
            List<GroupStandingDTO> standings =
                    groupService.calculateGroupStandings(group.getId());

            addPlayerFromStanding(standings, 0, firstPlacePlayers);
            addPlayerFromStanding(standings, 1, secondPlacePlayers);
        }

        List<Player> qualifiedPlayers = new ArrayList<>(firstPlacePlayers);
        qualifiedPlayers.addAll(secondPlacePlayers);

        return qualifiedPlayers;
    }

    public int calculateRequiredByes(
            int totalQualifiedPlayers,
            int targetBracketSize) {

        validateBracketSize(targetBracketSize);

        if (totalQualifiedPlayers > targetBracketSize) {
            throw new BusinessException(
                    "O número de classificados não pode ser maior que o tamanho da chave."
            );
        }

        return targetBracketSize - totalQualifiedPlayers;
    }

    public List<Player> determinePlayersWithBye(
            Long categoryId,
            int totalQualifiedPlayers,
            int targetBracketSize) {

        int numberOfByes = calculateRequiredByes(
                totalQualifiedPlayers,
                targetBracketSize
        );

        if (numberOfByes == 0) {
            return new ArrayList<>();
        }

        List<Player> groupWinners = getOrderedGroupWinners(categoryId);
        List<Player> playersWithBye = new ArrayList<>();

        // 1º: distribuir BYEs aos vencedores dos grupos
        for (Player winner : groupWinners) {
            if (playersWithBye.size() >= numberOfByes) {
                break;
            }

            playersWithBye.add(winner);
        }

        // 2º: completar com os melhores segundos colocados
        if (playersWithBye.size() < numberOfByes) {
            List<Player> secondPlacePlayers =
                    getOrderedSecondPlacePlayers(categoryId);

            for (Player player : secondPlacePlayers) {
                if (playersWithBye.size() >= numberOfByes) {
                    break;
                }

                playersWithBye.add(player);
            }
        }

        if (playersWithBye.size() < numberOfByes) {
            throw new BusinessException(
                    "Não existem jogadores classificados suficientes para distribuir todos os Byes."
            );
        }

        return playersWithBye;
    }

    private List<Group> getOrderedGroups(Long categoryId) {

        List<Group> groups = groupRepository.findByCategoryId(categoryId);

        if (groups.isEmpty()) {
            throw new BusinessException(
                    "Não existem grupos para esta categoria."
            );
        }

        groups.sort(
                Comparator.comparing(
                        Group::getName,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return groups;
    }

    private void addPlayerFromStanding(
            List<GroupStandingDTO> standings,
            int position,
            List<Player> destination) {

        if (standings.size() <= position) {
            return;
        }

        Long playerId = standings.get(position).getPlayerId();

        playerRepository.findById(playerId)
                .ifPresent(destination::add);
    }

    private Category findCategory(Long categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(
                        "Categoria não encontrada com ID: " + categoryId
                ));
    }

    private void validateBracketSize(int bracketSize) {

        if (bracketSize != 2
                && bracketSize != 4
                && bracketSize != 8
                && bracketSize != 16
                && bracketSize != 32) {

            throw new BusinessException(
                    "Tamanho de chave inválido para gerar Playoffs."
            );
        }
    }

    private MatchPhase determineFirstRoundPhase(int bracketSize) {

        return switch (bracketSize) {
            case 32 -> MatchPhase.ROUND_OF_32;
            case 16 -> MatchPhase.ROUND_OF_16;
            case 8 -> MatchPhase.QUARTER_FINAL;
            case 4 -> MatchPhase.SEMI_FINAL;
            case 2 -> MatchPhase.FINAL;
            default -> throw new BusinessException(
                    "Tamanho de chave inválido para gerar Playoffs."
            );
        };
    }

    private MatchPhase getNextPhase(MatchPhase currentPhase) {

        return switch (currentPhase) {
            case ROUND_OF_32 -> MatchPhase.ROUND_OF_16;
            case ROUND_OF_16 -> MatchPhase.QUARTER_FINAL;
            case QUARTER_FINAL -> MatchPhase.SEMI_FINAL;
            case SEMI_FINAL -> MatchPhase.FINAL;
            default -> null;
        };
    }

    private List<Match> createFirstRoundKnockoutMatches(
            Category category,
            List<Player> orderedPlayers,
            List<Player> playersWithBye,
            int bracketSize) {

        List<Match> matches = new ArrayList<>();

        int numberOfMatches = bracketSize / 2;

        MatchPhase firstRoundPhase =
                determineFirstRoundPhase(bracketSize);

        List<Player> remainingPlayers = new ArrayList<>(orderedPlayers);

        for (Player byePlayer : playersWithBye) {
            remainingPlayers.removeIf(
                    player -> player.getId().equals(byePlayer.getId())
            );
        }

        List<Integer> byeMatchIndexes = new ArrayList<>();

        for (int i = 0; i < numberOfMatches; i += 2) {
            byeMatchIndexes.add(i);
        }

        for (int i = 1; i < numberOfMatches; i += 2) {
            byeMatchIndexes.add(i);
        }

        int byeIndex = 0;
        int playerIndex = 0;

        for (int i = 0; i < numberOfMatches; i++) {

            Match match = new Match();

            match.setCategory(category);
            match.setPhase(firstRoundPhase);
            match.setStatus(MatchStatus.SCHEDULED);

            if (byeIndex < playersWithBye.size()
                    && byeMatchIndexes.get(byeIndex) == i) {

                Player byePlayer = playersWithBye.get(byeIndex);

                match.setPlayer1(byePlayer);
                match.setPlayer2(null);
                match.setStatus(MatchStatus.FINISHED);

                byeIndex++;

            } else {

                Player player1 = getPlayerAt(
                        remainingPlayers,
                        playerIndex
                );

                Player player2 = getPlayerAt(
                        remainingPlayers,
                        playerIndex + 1
                );

                match.setPlayer1(player1);
                match.setPlayer2(player2);

                playerIndex += 2;
            }

            matches.add(matchRepository.save(match));
        }

        return matches;
    }

    private List<Match> createSubsequentRoundMatches(
            Category category,
            int bracketSize) {

        List<Match> matches = new ArrayList<>();

        int matchesInRound = bracketSize / 4;
        MatchPhase currentPhase =
                determineFirstRoundPhase(bracketSize);

        while (currentPhase != MatchPhase.FINAL) {

            currentPhase = getNextPhase(currentPhase);

            if (currentPhase == null) {
                break;
            }

            for (int i = 0; i < matchesInRound; i++) {
                Match match = new Match();
                match.setCategory(category);
                match.setPhase(currentPhase);
                match.setStatus(MatchStatus.SCHEDULED);

                matches.add(matchRepository.save(match));
            }

            matchesInRound /= 2;
        }

        return matches;
    }

    private Player getPlayerAt(List<Player> players, int index) {

        if (index < 0 || index >= players.size()) {
            return null;
        }

        return players.get(index);
    }

    private MatchResponseDTO mapMatchToResponseDTO(Match match) {

        MatchResponseDTO dto = new MatchResponseDTO();

        dto.setId(match.getId());
        dto.setCategoryId(match.getCategory().getId());
        dto.setCategoryName(match.getCategory().getName());

        if (match.getPlayer1() != null) {
            dto.setPlayer1(toPlayerSummary(match.getPlayer1()));
        }

        if (match.getPlayer2() != null) {
            dto.setPlayer2(toPlayerSummary(match.getPlayer2()));
        }

        dto.setScorePlayer1(match.getScorePlayer1());
        dto.setScorePlayer2(match.getScorePlayer2());
        dto.setStatus(match.getStatus());
        dto.setPhase(match.getPhase());
        dto.setTableOrCourt(match.getTableOrCourt());
        dto.setScheduledTime(match.getScheduledTime());

        return dto;
    }

    private PlayerSummaryDTO toPlayerSummary(Player player) {

        return new PlayerSummaryDTO(
                player.getId(),
                player.getName(),
                player.getClubAcademy()
        );
    }
}