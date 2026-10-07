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
    public BracketResponseDTO generateKnockoutBracket(Long categoryId) {

        Category category = findCategory(categoryId);
        List<Group> groups = getOrderedGroups(categoryId);

        int totalQualifiedPlayers = groups.size() * 2;
        int bracketSize = calculateBracketSize(totalQualifiedPlayers);

        List<Player> firstPlacePlayers =
                getPlayersByStandingPosition(categoryId, 0);

        List<Player> secondPlacePlayers =
                getPlayersByStandingPosition(categoryId, 1);

        int numberOfByes =
                calculateRequiredByes(
                        totalQualifiedPlayers,
                        bracketSize
                );

        List<Player> playersWithBye =
                determinePlayersWithBye(
                        firstPlacePlayers,
                        secondPlacePlayers,
                        numberOfByes
                );

        List<Match> matches =
                createFirstRoundKnockoutMatches(
                        category,
                        firstPlacePlayers,
                        secondPlacePlayers,
                        playersWithBye,
                        bracketSize
                );

        matches.addAll(
                createSubsequentRoundMatches(
                        category,
                        bracketSize
                )
        );

        advancePlayersWithBye(matches, bracketSize);

        return buildBracketResponse(
                category,
                bracketSize,
                matches
        );
    }

    private BracketResponseDTO buildBracketResponse(
            Category category,
            int bracketSize,
            List<Match> matches) {

        BracketResponseDTO response =
                new BracketResponseDTO();

        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setTargetBracketSize(bracketSize);

        response.setMatches(
                matches.stream()
                        .map(this::mapMatchToResponseDTO)
                        .toList()
        );

        return response;
    }

    private int calculateBracketSize(int totalQualifiedPlayers) {

        if (totalQualifiedPlayers <= 2) {
            return 2;
        }

        int bracketSize = 2;

        while (bracketSize < totalQualifiedPlayers) {
            bracketSize *= 2;
        }

        return bracketSize;
    }

    private List<Player> determinePlayersWithBye(
            List<Player> firstPlacePlayers,
            List<Player> secondPlacePlayers,
            int numberOfByes) {

        List<Player> playersWithBye = new ArrayList<>();

        int firstPlaceByes =
                Math.min(numberOfByes, firstPlacePlayers.size());

        playersWithBye.addAll(
                firstPlacePlayers.subList(0, firstPlaceByes)
        );

        int remainingByes =
                numberOfByes - playersWithBye.size();

        if (remainingByes == 0) {
            return playersWithBye;
        }

        if (remainingByes > secondPlacePlayers.size()) {
            throw new BusinessException(
                    "Não existem jogadores classificados suficientes para distribuir todos os Byes."
            );
        }

        if (firstPlacePlayers.size() == 5
                && remainingByes == 1) {

            playersWithBye.add(
                    secondPlacePlayers.get(1)
            );

            return playersWithBye;
        }

        playersWithBye.addAll(
                secondPlacePlayers.subList(0, remainingByes)
        );

        return playersWithBye;
    }

    private void advancePlayersWithBye(
            List<Match> matches,
            int bracketSize) {

        MatchPhase firstRoundPhase =
                determineFirstRoundPhase(bracketSize);

        MatchPhase nextPhase =
                getNextPhase(firstRoundPhase);

        if (nextPhase == null) {
            return;
        }

        List<Match> firstRoundMatches =
                getMatchesByPhase(matches, firstRoundPhase);

        List<Match> nextRoundMatches =
                getMatchesByPhase(matches, nextPhase);

        for (int i = 0; i < firstRoundMatches.size(); i++) {

            Player winner =
                    getByeWinner(firstRoundMatches.get(i));

            if (winner == null) {
                continue;
            }

            int targetMatchIndex = i / 2;

            if (targetMatchIndex >= nextRoundMatches.size()) {
                continue;
            }

            Match targetMatch =
                    nextRoundMatches.get(targetMatchIndex);

            setByeWinner(
                    targetMatch,
                    winner,
                    i
            );

            matchRepository.save(targetMatch);
        }
    }

    private List<Match> getMatchesByPhase(
            List<Match> matches,
            MatchPhase phase) {

        return matches.stream()
                .filter(match -> match.getPhase() == phase)
                .sorted(Comparator.comparing(Match::getId))
                .toList();
    }

    private void setByeWinner(
            Match targetMatch,
            Player winner,
            int firstRoundIndex) {

        if (firstRoundIndex % 2 == 0) {
            targetMatch.setPlayer1(winner);
            return;
        }

        targetMatch.setPlayer2(winner);
    }

    private Player getByeWinner(Match match) {

        if (match.getStatus() != MatchStatus.FINISHED) {
            return null;
        }

        if (match.getPlayer1() != null
                && match.getPlayer2() == null) {

            return match.getPlayer1();
        }

        if (match.getPlayer2() != null
                && match.getPlayer1() == null) {

            return match.getPlayer2();
        }

        return null;
    }

    public List<Player> getOrderedGroupWinners(Long categoryId) {
        return getPlayersByStandingPosition(categoryId, 0);
    }

    private List<Player> getOrderedGroupSecondPlaces(Long categoryId) {
        return getPlayersByStandingPosition(categoryId, 1);
    }

    private List<Player> getPlayersByStandingPosition(
            Long categoryId,
            int position) {

        List<Group> groups =
                getOrderedGroups(categoryId);

        List<Player> players = new ArrayList<>();

        for (Group group : groups) {

            List<GroupStandingDTO> standings =
                    groupService.calculateGroupStandings(
                            group.getId()
                    );

            addPlayerFromStanding(
                    standings,
                    position,
                    players
            );
        }

        return players;
    }

    public List<Player> getOrderedQualifiedPlayers(Long categoryId) {

        List<Player> firstPlaces =
                getOrderedGroupWinners(categoryId);

        List<Player> secondPlaces =
                getOrderedGroupSecondPlaces(categoryId);

        List<Player> qualifiedPlayers =
                new ArrayList<>(firstPlaces);

        qualifiedPlayers.addAll(secondPlaces);

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

    private List<Group> getOrderedGroups(Long categoryId) {

        List<Group> groups =
                groupRepository.findByCategoryId(categoryId);

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

        Long playerId =
                standings.get(position).getPlayerId();

        playerRepository.findById(playerId)
                .ifPresent(destination::add);
    }

    private Category findCategory(Long categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Categoria não encontrada com ID: "
                                        + categoryId
                        )
                );
    }

    private void validateBracketSize(int bracketSize) {

        switch (bracketSize) {
            case 2, 4, 8, 16, 32 -> {
                return;
            }
            default -> throw new BusinessException(
                    "Tamanho de chave inválido para gerar Playoffs."
            );
        }
    }

    private MatchPhase determineFirstRoundPhase(
            int bracketSize) {

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

    private MatchPhase getNextPhase(
            MatchPhase currentPhase) {

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
            List<Player> firstPlacePlayers,
            List<Player> secondPlacePlayers,
            List<Player> playersWithBye,
            int bracketSize) {

        List<Match> matches = new ArrayList<>();

        MatchPhase firstRoundPhase =
                determineFirstRoundPhase(bracketSize);

        List<Player> seededPlayers =
                buildSeededPlayers(
                        firstPlacePlayers,
                        secondPlacePlayers,
                        bracketSize
                );

        int numberOfMatches = bracketSize / 2;

        for (int i = 0; i < numberOfMatches; i++) {

            Player player1 =
                    getPlayerAt(seededPlayers, i * 2);

            Player player2 =
                    getPlayerAt(seededPlayers, i * 2 + 1);

            Match match =
                    createFirstRoundMatch(
                            category,
                            firstRoundPhase,
                            player1,
                            player2,
                            playersWithBye
                    );

            matches.add(
                    matchRepository.save(match)
            );
        }

        return matches;
    }

    private Match createFirstRoundMatch(
            Category category,
            MatchPhase phase,
            Player player1,
            Player player2,
            List<Player> playersWithBye) {

        Match match = new Match();

        match.setCategory(category);
        match.setPhase(phase);
        match.setPlayer1(player1);
        match.setPlayer2(player2);

        if (player1 != null
                && playersWithBye.contains(player1)) {

            match.setPlayer2(null);
            match.setStatus(MatchStatus.FINISHED);

        } else if (player2 != null
                && playersWithBye.contains(player2)) {

            match.setPlayer1(player2);
            match.setPlayer2(null);
            match.setStatus(MatchStatus.FINISHED);

        } else {

            match.setStatus(MatchStatus.SCHEDULED);
        }

        return match;
    }

    private List<Player> buildSeededPlayers(
            List<Player> firstPlacePlayers,
            List<Player> secondPlacePlayers,
            int bracketSize) {

        List<Player> slots =
                new ArrayList<>(
                        java.util.Collections.nCopies(
                                bracketSize,
                                null
                        )
                );

        int groups = firstPlacePlayers.size();

        if (groups == 0) {
            return slots;
        }

        if (groups == 1) {
            slots.set(0, firstPlacePlayers.get(0));

            if (!secondPlacePlayers.isEmpty()) {
                slots.set(1, secondPlacePlayers.get(0));
            }

            return slots;
        }

        assignPlayersToSlots(
                slots,
                firstPlacePlayers,
                getFirstPlacePositions(groups)
        );

        assignPlayersToSlots(
                slots,
                secondPlacePlayers,
                getSecondPlacePositions(groups)
        );

        return slots;
    }

    private void assignPlayersToSlots(
            List<Player> slots,
            List<Player> players,
            int[] positions) {

        for (int i = 0;
             i < positions.length && i < players.size();
             i++) {

            int position = positions[i];

            if (position >= 0
                    && position < slots.size()
                    && slots.get(position) == null) {

                slots.set(
                        position,
                        players.get(i)
                );
            }
        }
    }

    private int[] getFirstPlacePositions(int groups) {

        return switch (groups) {
            case 2 -> new int[]{0, 3};
            case 3 -> new int[]{0, 4, 6};
            case 4 -> new int[]{0, 6, 4, 2};
            case 5 -> new int[]{0, 6, 12, 10, 4};
            case 6 -> new int[]{0, 6, 12, 14, 8, 2};
            case 7 -> new int[]{0, 14, 8, 6, 10, 4, 12};
            case 8 -> new int[]{0, 14, 8, 6, 10, 4, 12, 2};
            default -> throw new BusinessException(
                    "Quantidade de grupos não suportada para geração do chaveamento."
            );
        };
    }

    private int[] getSecondPlacePositions(int groups) {

        return switch (groups) {
            case 2 -> new int[]{2, 1};
            case 3 -> new int[]{3, 7, 2};
            case 4 -> new int[]{5, 3, 1, 7};
            case 5 -> new int[]{14, 8, 2, 3, 15};
            case 6 -> new int[]{9, 10, 3, 4, 5, 11};

            // 7 grupos: posições 1 e 15 ficam livres para os BYEs.
            case 7 -> new int[]{2, 3, 5, 7, 9, 11, 13};

            case 8 -> new int[]{11, 5, 7, 13, 3, 9, 1, 15};
            default -> throw new BusinessException(
                    "Quantidade de grupos não suportada para geração do chaveamento."
            );
        };
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

                matches.add(
                        matchRepository.save(match)
                );
            }

            matchesInRound /= 2;
        }

        return matches;
    }

    private Player getPlayerAt(
            List<Player> players,
            int index) {

        if (index < 0 || index >= players.size()) {
            return null;
        }

        return players.get(index);
    }

    private MatchResponseDTO mapMatchToResponseDTO(
            Match match) {

        MatchResponseDTO dto =
                new MatchResponseDTO();

        dto.setId(match.getId());
        dto.setCategoryId(match.getCategory().getId());
        dto.setCategoryName(match.getCategory().getName());

        if (match.getPlayer1() != null) {
            dto.setPlayer1(
                    toPlayerSummary(match.getPlayer1())
            );
        }

        if (match.getPlayer2() != null) {
            dto.setPlayer2(
                    toPlayerSummary(match.getPlayer2())
            );
        }

        dto.setScorePlayer1(match.getScorePlayer1());
        dto.setScorePlayer2(match.getScorePlayer2());
        dto.setStatus(match.getStatus());
        dto.setPhase(match.getPhase());
        dto.setTableOrCourt(match.getTableOrCourt());
        dto.setScheduledTime(match.getScheduledTime());

        return dto;
    }

    private PlayerSummaryDTO toPlayerSummary(
            Player player) {

        return new PlayerSummaryDTO(
                player.getId(),
                player.getName(),
                player.getClubAcademy()
        );
    }
}