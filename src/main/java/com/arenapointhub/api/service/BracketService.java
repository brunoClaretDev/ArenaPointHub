
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

        List<Player> firstPlaces = getPlayersByStandingPosition(categoryId, 0);
        List<Player> secondPlaces = getPlayersByStandingPosition(categoryId, 1);

        validateQualifiedPlayers(groups.size(), firstPlaces, secondPlaces);

        int totalQualified = firstPlaces.size() + secondPlaces.size();
        int bracketSize = calculateBracketSize(totalQualified);
        int numberOfByes = calculateRequiredByes(totalQualified, bracketSize);

        List<Player> playersWithBye =
                determinePlayersWithBye(firstPlaces, secondPlaces, numberOfByes);

        List<Player> seededPlayers =
                buildSeededPlayers(firstPlaces, secondPlaces, bracketSize);

        alignByePlayersWithEmptySlots(seededPlayers, playersWithBye);

        List<Match> matches = createFirstRoundMatches(
                category, seededPlayers, bracketSize);

        matches.addAll(createSubsequentRoundMatches(category, bracketSize));
        advancePlayersWithBye(matches, bracketSize);

        return buildBracketResponse(category, bracketSize, matches);
    }

    private void validateQualifiedPlayers(
            int groupCount,
            List<Player> firstPlaces,
            List<Player> secondPlaces) {
        if (groupCount < 1 || groupCount > 8) {
            throw new BusinessException(
                    "A geração do chaveamento suporta de 1 a 8 grupos.");
        }

        if (firstPlaces.size() != groupCount
                || secondPlaces.size() != groupCount) {
            throw new BusinessException(
                    "Cada grupo precisa ter dois jogadores classificados.");
        }

        List<Long> ids = new ArrayList<>();
        firstPlaces.forEach(player -> ids.add(player.getId()));
        secondPlaces.forEach(player -> ids.add(player.getId()));

        if (ids.stream().distinct().count() != ids.size()) {
            throw new BusinessException(
                    "Existem jogadores duplicados entre os classificados.");
        }
    }

    private BracketResponseDTO buildBracketResponse(
            Category category,
            int bracketSize,
            List<Match> matches) {
        BracketResponseDTO response = new BracketResponseDTO();
        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setTargetBracketSize(bracketSize);
        response.setMatches(
                matches.stream().map(this::mapMatchToResponseDTO).toList());
        return response;
    }

    private int calculateBracketSize(int totalPlayers) {
        if (totalPlayers <= 2) {
            return 2;
        }

        int size = 2;
        while (size < totalPlayers) {
            size *= 2;
        }
        return size;
    }


	public int calculateRequiredByes(int totalPlayers, int bracketSize) {
	    validateBracketSize(bracketSize);
	
	    if (totalPlayers > bracketSize) {
	        throw new BusinessException(
	                "O número de classificados não pode ser maior que o tamanho da chave.");
	    }
	
	    return bracketSize - totalPlayers;
	}


    private List<Player> determinePlayersWithBye(
            List<Player> firstPlaces,
            List<Player> secondPlaces,
            int numberOfByes) {
        List<Player> result = new ArrayList<>();

        int firstPlaceByes = Math.min(numberOfByes, firstPlaces.size());
        result.addAll(firstPlaces.subList(0, firstPlaceByes));

        int remaining = numberOfByes - result.size();

        if (remaining > 0) {
            if (firstPlaces.size() == 5 && remaining == 1) {
                result.add(secondPlaces.get(1));
            } else {
                result.addAll(secondPlaces.subList(0, remaining));
            }
        }

        return result;
    }

    private List<Match> createFirstRoundMatches(
            Category category,
            List<Player> seededPlayers,
            int bracketSize) {
        List<Match> matches = new ArrayList<>();
        MatchPhase phase = determineFirstRoundPhase(bracketSize);

        for (int i = 0; i < bracketSize; i += 2) {
            Player player1 = seededPlayers.get(i);
            Player player2 = seededPlayers.get(i + 1);

            if (player1 == null && player2 == null) {
                throw new BusinessException(
                        "O chaveamento contém uma partida sem jogadores.");
            }

            Match match = new Match();
            match.setCategory(category);
            match.setPhase(phase);

            if ((player1 == null) != (player2 == null)) {
                match.setPlayer1(player1 != null ? player1 : player2);
                match.setStatus(MatchStatus.FINISHED);
            } else {
                match.setPlayer1(player1);
                match.setPlayer2(player2);
                match.setStatus(MatchStatus.SCHEDULED);
            }

            matches.add(matchRepository.save(match));
        }

        return matches;
    }

    private void alignByePlayersWithEmptySlots(
            List<Player> seededPlayers,
            List<Player> playersWithBye) {
        for (Player byePlayer : playersWithBye) {
            int index = seededPlayers.indexOf(byePlayer);

            if (index < 0) {
                throw new BusinessException(
                        "Jogador selecionado para BYE não está no chaveamento.");
            }

            int opponentIndex = index ^ 1;

            if (seededPlayers.get(opponentIndex) != null) {
                throw new BusinessException(
                        "A distribuição dos BYEs não corresponde às vagas livres.");
            }
        }
    }

    private void advancePlayersWithBye(
            List<Match> matches,
            int bracketSize) {
        MatchPhase firstPhase = determineFirstRoundPhase(bracketSize);
        MatchPhase nextPhase = getNextPhase(firstPhase);

        if (nextPhase == null) {
            return;
        }

        List<Match> firstRound = getMatchesByPhase(matches, firstPhase);
        List<Match> nextRound = getMatchesByPhase(matches, nextPhase);

        for (int i = 0; i < firstRound.size(); i++) {
            Player winner = getByeWinner(firstRound.get(i));

            if (winner == null) {
                continue;
            }

            int targetIndex = i / 2;
            if (targetIndex >= nextRound.size()) {
                throw new BusinessException(
                        "Não foi encontrada a partida da próxima fase para o BYE.");
            }

            Match target = nextRound.get(targetIndex);

            if (i % 2 == 0) {
                target.setPlayer1(winner);
            } else {
                target.setPlayer2(winner);
            }

            matchRepository.save(target);
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

    private Player getByeWinner(Match match) {
        if (match.getStatus() != MatchStatus.FINISHED) {
            return null;
        }

        if (match.getPlayer1() != null && match.getPlayer2() == null) {
            return match.getPlayer1();
        }

        if (match.getPlayer2() != null && match.getPlayer1() == null) {
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

    public List<Player> getOrderedQualifiedPlayers(Long categoryId) {
        List<Player> result = new ArrayList<>(
                getOrderedGroupWinners(categoryId));
        result.addAll(getOrderedGroupSecondPlaces(categoryId));
        return result;
    }

    private List<Player> getPlayersByStandingPosition(
            Long categoryId,
            int position) {
        List<Player> result = new ArrayList<>();

        for (Group group : getOrderedGroups(categoryId)) {
            List<GroupStandingDTO> standings =
                    groupService.calculateGroupStandings(group.getId());

            if (standings.size() <= position) {
                continue;
            }

            Long playerId = standings.get(position).getPlayerId();
            playerRepository.findById(playerId).ifPresent(result::add);
        }

        return result;
    }

    private List<Group> getOrderedGroups(Long categoryId) {
        List<Group> groups = new ArrayList<>(
                groupRepository.findByCategoryId(categoryId));

        if (groups.isEmpty()) {
            throw new BusinessException(
                    "Não existem grupos para esta categoria.");
        }

        groups.sort(
                Comparator.comparing(
                        Group::getName,
                        String.CASE_INSENSITIVE_ORDER));

        return groups;
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(
                        "Categoria não encontrada com ID: " + categoryId));
    }

    private void validateBracketSize(int bracketSize) {
        switch (bracketSize) {
            case 2, 4, 8, 16, 32 -> {
                return;
            }
            default -> throw new BusinessException(
                    "Tamanho de chave inválido para gerar Playoffs.");
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
                    "Tamanho de chave inválido para gerar Playoffs.");
        };
    }

    private MatchPhase getNextPhase(MatchPhase phase) {
        return switch (phase) {
            case ROUND_OF_32 -> MatchPhase.ROUND_OF_16;
            case ROUND_OF_16 -> MatchPhase.QUARTER_FINAL;
            case QUARTER_FINAL -> MatchPhase.SEMI_FINAL;
            case SEMI_FINAL -> MatchPhase.FINAL;
            default -> null;
        };
    }

    private List<Player> buildSeededPlayers(
            List<Player> firstPlaces,
            List<Player> secondPlaces,
            int bracketSize) {
        List<Player> slots = new ArrayList<>(
                java.util.Collections.nCopies(bracketSize, null));

        int groups = firstPlaces.size();

        if (groups == 1) {
            slots.set(0, firstPlaces.get(0));
            slots.set(1, secondPlaces.get(0));
            return slots;
        }

        assignPlayersToSlots(
                slots, firstPlaces, getFirstPlacePositions(groups));
        assignPlayersToSlots(
                slots, secondPlaces, getSecondPlacePositions(groups));

        return slots;
    }

    private void assignPlayersToSlots(
            List<Player> slots,
            List<Player> players,
            int[] positions) {
        if (positions.length != players.size()) {
            throw new BusinessException(
                    "A quantidade de posições não corresponde aos classificados.");
        }

        for (int i = 0; i < positions.length; i++) {
            int position = positions[i];

            if (position < 0 || position >= slots.size()
                    || slots.get(position) != null) {
                throw new BusinessException(
                        "Posição inválida ou duplicada no chaveamento.");
            }

            slots.set(position, players.get(i));
        }
    }

    private int[] getFirstPlacePositions(int groups) {
        return switch (groups) {
            case 2 -> new int[] { 0, 3 };
            case 3 -> new int[] { 0, 4, 6 };
            case 4 -> new int[] { 0, 6, 4, 2 };
            case 5 -> new int[] { 0, 6, 12, 10, 4 };
            case 6 -> new int[] { 0, 6, 12, 14, 8, 2 };
            case 7 -> new int[] { 0, 14, 8, 6, 10, 4, 12 };
            case 8 -> new int[] { 0, 14, 8, 6, 10, 4, 12, 2 };
            default -> throw new BusinessException(
                    "Quantidade de grupos não suportada para geração do chaveamento.");
        };
    }

    private int[] getSecondPlacePositions(int groups) {
        return switch (groups) {
            case 2 -> new int[] { 2, 1 };
            case 3 -> new int[] { 7, 2, 3 };
            case 4 -> new int[] { 5, 3, 1, 7 };
            case 5 -> new int[] { 14, 8, 2, 3, 15 };
            case 6 -> new int[] { 9, 10, 3, 4, 5, 11 };
            case 7 -> new int[] { 2, 3, 5, 7, 9, 11, 13 };
            case 8 -> new int[] { 11, 5, 7, 13, 3, 9, 1, 15 };
            default -> throw new BusinessException(
                    "Quantidade de grupos não suportada para geração do chaveamento.");
        };
    }

    private List<Match> createSubsequentRoundMatches(
            Category category,
            int bracketSize) {
        List<Match> matches = new ArrayList<>();

        int matchesInRound = bracketSize / 4;
        MatchPhase phase = determineFirstRoundPhase(bracketSize);

        while (phase != MatchPhase.FINAL) {
            phase = getNextPhase(phase);

            if (phase == null) {
                break;
            }

            for (int i = 0; i < matchesInRound; i++) {
                Match match = new Match();
                match.setCategory(category);
                match.setPhase(phase);
                match.setStatus(MatchStatus.SCHEDULED);
                matches.add(matchRepository.save(match));
            }

            matchesInRound /= 2;
        }

        return matches;
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
                player.getClubAcademy());
    }
}
