package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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

    private static final int MAX_BRACKET_SIZE = 64;

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

    // =========================================================
    // GERAÇÃO DO CHAVEAMENTO
    // =========================================================

    @Transactional
    public BracketResponseDTO generateKnockoutBracket(Long categoryId) {

        Category category = findCategory(categoryId);

        validateKnockoutCanBeRegenerated(categoryId);
        
        List<Group> groups = getOrderedGroups(categoryId);

        List<Player> firstPlaces =
                getPlayersByStandingPosition(groups, 0);

        List<Player> secondPlaces =
                getPlayersByStandingPosition(groups, 1);

        validateQualifiedPlayers(
                groups.size(),
                firstPlaces,
                secondPlaces);

        int totalQualified = firstPlaces.size() + secondPlaces.size();

        int bracketSize = calculateBracketSize(totalQualified);

        int numberOfByes = calculateRequiredByes(
                totalQualified,
                bracketSize);

        List<Player> playersWithBye = determinePlayersWithBye(
                firstPlaces,
                secondPlaces,
                numberOfByes);

        List<Player> seededPlayers = buildSeededPlayers(
                firstPlaces,
                secondPlaces,
                playersWithBye,
                bracketSize);

        // Somente remove o chaveamento anterior depois de validar
        // todos os classificados e preparar a nova distribuição.
        removeExistingKnockoutIfNotStarted(categoryId);

        List<Match> matches = new ArrayList<>();

        matches.addAll(
                createFirstRoundMatches(
                        category,
                        seededPlayers,
                        bracketSize));

        matches.addAll(
                createSubsequentRoundMatches(
                        category,
                        bracketSize));

        advancePlayersWithBye(matches, bracketSize);

        return buildBracketResponse(
                category,
                bracketSize,
                matches);
    }

    // =========================================================
    // VALIDAÇÃO DOS CLASSIFICADOS
    // =========================================================

    private void validateQualifiedPlayers(
            int groupCount,
            List<Player> firstPlaces,
            List<Player> secondPlaces) {

        if (groupCount < 1) {
            throw new BusinessException(
                    "É necessário pelo menos um grupo para gerar o chaveamento.");
        }

        if (firstPlaces.size() != groupCount
                || secondPlaces.size() != groupCount) {

            throw new BusinessException(
                    "Cada grupo precisa ter dois jogadores classificados.");
        }

        List<Long> playerIds = new ArrayList<>();

        for (Player player : firstPlaces) {
            if (player == null || player.getId() == null) {
                throw new BusinessException(
                        "Existe um jogador inválido entre os primeiros colocados.");
            }

            playerIds.add(player.getId());
        }

        for (Player player : secondPlaces) {
            if (player == null || player.getId() == null) {
                throw new BusinessException(
                        "Existe um jogador inválido entre os segundos colocados.");
            }

            playerIds.add(player.getId());
        }

        if (playerIds.stream().distinct().count() != playerIds.size()) {
            throw new BusinessException(
                    "Existem jogadores duplicados entre os classificados.");
        }
    }

    // =========================================================
    // TAMANHO DA CHAVE
    // =========================================================

    private int calculateBracketSize(int totalPlayers) {

        if (totalPlayers < 2) {
            throw new BusinessException(
                    "São necessários pelo menos dois classificados para gerar o mata-mata.");
        }

        int size = 2;

        while (size < totalPlayers) {

            if (size >= MAX_BRACKET_SIZE) {
                throw new BusinessException(
                        "A chave suporta no máximo 64 classificados.");
            }

            size *= 2;
        }

        validateBracketSize(size);

        return size;
    }

    public int calculateRequiredByes(
            int totalPlayers,
            int bracketSize) {

        validateBracketSize(bracketSize);

        if (totalPlayers < 2 || totalPlayers > bracketSize) {
        	throw new BusinessException(
        	        "O número de classificados não pode ser maior que o tamanho da chave.");
        }

        return bracketSize - totalPlayers;
    }

    private void validateBracketSize(int bracketSize) {

        if (bracketSize != 2
                && bracketSize != 4
                && bracketSize != 8
                && bracketSize != 16
                && bracketSize != 32
                && bracketSize != 64) {

            throw new BusinessException(
                    "Tamanho de chave inválido para gerar Playoffs.");
        }
    }

    // =========================================================
    // DISTRIBUIÇÃO DOS BYES
    // =========================================================

    private List<Player> determinePlayersWithBye(
            List<Player> firstPlaces,
            List<Player> secondPlaces,
            int numberOfByes) {

        List<Player> result = new ArrayList<>();

        if (numberOfByes < 0
                || numberOfByes > firstPlaces.size() + secondPlaces.size()) {

            throw new BusinessException(
                    "Quantidade inválida de BYEs.");
        }

        // Prioridade: primeiros colocados, seguindo a ordem dos grupos.
        int firstPlaceByes = Math.min(
                numberOfByes,
                firstPlaces.size());

        result.addAll(
                firstPlaces.subList(0, firstPlaceByes));

        // Somente depois são contemplados os segundos colocados.
        int remainingByes = numberOfByes - result.size();

        if (remainingByes > 0) {

            int secondPlaceByes = Math.min(
                    remainingByes,
                    secondPlaces.size());

            result.addAll(
                    secondPlaces.subList(0, secondPlaceByes));
        }

        if (result.size() != numberOfByes) {
            throw new BusinessException(
                    "Não foi possível distribuir todos os BYEs.");
        }

        return result;
    }

    // =========================================================
    // MONTAGEM DINÂMICA DO CHAVEAMENTO
    // =========================================================

    private List<Player> buildSeededPlayers(
            List<Player> firstPlaces,
            List<Player> secondPlaces,
            List<Player> playersWithBye,
            int bracketSize) {

        int groupCount = firstPlaces.size();
        int halfSize = bracketSize / 2;

        List<Player> slots = new ArrayList<>(
                java.util.Collections.nCopies(bracketSize, null));

        if (groupCount == 1) {
            slots.set(0, firstPlaces.get(0));
            slots.set(1, secondPlaces.get(0));
            return slots;
        }
        
        if (groupCount == 3) {
            slots.set(0, firstPlaces.get(0));

            slots.set(2, secondPlaces.get(1));
            slots.set(3, secondPlaces.get(2));

            slots.set(4, firstPlaces.get(1));

            slots.set(6, firstPlaces.get(2));
            slots.set(7, secondPlaces.get(0));

            validateSeededPlayers(slots, firstPlaces, secondPlaces);
            return slots;
        }
        
        if (groupCount == 4) {
            slots.set(0, firstPlaces.get(0));
            slots.set(1, secondPlaces.get(2));

            slots.set(2, firstPlaces.get(3));
            slots.set(3, secondPlaces.get(1));

            slots.set(4, firstPlaces.get(2));
            slots.set(5, secondPlaces.get(0));

            slots.set(6, firstPlaces.get(1));
            slots.set(7, secondPlaces.get(3));

            validateSeededPlayers(slots, firstPlaces, secondPlaces);
            return slots;
        }
        
        if (groupCount == 6) {
            slots.set(0, firstPlaces.get(0));

            slots.set(2, firstPlaces.get(5));
            slots.set(3, secondPlaces.get(2));

            slots.set(4, secondPlaces.get(3));
            slots.set(5, secondPlaces.get(4));
            slots.set(6, firstPlaces.get(1));

            slots.set(8, firstPlaces.get(4));
            slots.set(9, secondPlaces.get(0));

            slots.set(10, secondPlaces.get(1));
            slots.set(11, secondPlaces.get(5));

            slots.set(12, firstPlaces.get(2));
            slots.set(14, firstPlaces.get(3));

            validateSeededPlayers(slots, firstPlaces, secondPlaces);
            return slots;
        }
        
        if (groupCount == 7) {
            slots.set(0, firstPlaces.get(0));

            slots.set(2, secondPlaces.get(0));
            slots.set(3, secondPlaces.get(1));

            slots.set(4, firstPlaces.get(5));
            slots.set(5, secondPlaces.get(2));

            slots.set(6, firstPlaces.get(3));
            slots.set(7, secondPlaces.get(4));

            slots.set(8, firstPlaces.get(2));
            slots.set(9, secondPlaces.get(3));

            slots.set(10, firstPlaces.get(4));
            slots.set(11, secondPlaces.get(6));

            slots.set(12, firstPlaces.get(6));
            slots.set(13, secondPlaces.get(5));

            slots.set(14, firstPlaces.get(1));

            validateSeededPlayers(slots, firstPlaces, secondPlaces);
            return slots;
        }
        
        if (groupCount == 8) {
            slots.set(0, firstPlaces.get(0));
            slots.set(1, secondPlaces.get(6));

            slots.set(2, firstPlaces.get(7));
            slots.set(3, secondPlaces.get(4));

            slots.set(4, firstPlaces.get(5));
            slots.set(5, secondPlaces.get(1));

            slots.set(6, firstPlaces.get(3));
            slots.set(7, secondPlaces.get(2));

            slots.set(8, firstPlaces.get(2));
            slots.set(9, secondPlaces.get(5));

            slots.set(10, firstPlaces.get(4));
            slots.set(11, secondPlaces.get(0));

            slots.set(12, firstPlaces.get(6));
            slots.set(13, secondPlaces.get(3));

            slots.set(14, firstPlaces.get(1));
            slots.set(15, secondPlaces.get(7));

            validateSeededPlayers(slots, firstPlaces, secondPlaces);
            return slots;
        }

        List<Player> upperHalf = new ArrayList<>();
        List<Player> lowerHalf = new ArrayList<>();

        for (int i = 0; i < groupCount; i++) {
            if (i % 2 == 0) {
                upperHalf.add(firstPlaces.get(i));
                lowerHalf.add(secondPlaces.get(i));
            } else {
                lowerHalf.add(firstPlaces.get(i));
                upperHalf.add(secondPlaces.get(i));
            }
        }

        List<Player> upperSlots = buildHalfSlots(
                upperHalf,
                playersWithBye,
                halfSize / 2);

        List<Player> lowerSlots = buildHalfSlots(
                lowerHalf,
                playersWithBye,
                halfSize / 2);

        for (int i = 0; i < halfSize; i++) {
            slots.set(i, upperSlots.get(i));
            slots.set(i + halfSize, lowerSlots.get(i));
        }

        validateSeededPlayers(slots, firstPlaces, secondPlaces);

        return slots;
    }

    private List<Player> buildHalfSlots(
            List<Player> halfPlayers,
            List<Player> playersWithBye,
            int matchesPerHalf) {

    	List<Player> byePlayers = playersWithBye.stream()
    	        .filter(bye -> halfPlayers.stream()
    	                .anyMatch(player -> player.getId().equals(bye.getId())))
    	        .toList();

    	List<Player> playersWithoutBye = halfPlayers.stream()
    	        .filter(player -> byePlayers.stream()
    	                .noneMatch(bye -> bye.getId().equals(player.getId())))
    	        .toList();
    	
    	if (playersWithoutBye.size() % 2 != 0) {
            throw new BusinessException(
                    "A distribuição dos classificados deixou um número ímpar de jogadores sem BYE em uma das metades.");
        }

        if (byePlayers.size() + playersWithoutBye.size() / 2
                != matchesPerHalf) {
            throw new BusinessException(
                    "A distribuição dos BYEs não corresponde à capacidade da metade do chaveamento.");
        }

        List<Player> slots = new ArrayList<>(
                java.util.Collections.nCopies(matchesPerHalf * 2, null));

        int matchIndex = 0;

        for (Player player : byePlayers) {
            slots.set(matchIndex * 2, player);
            matchIndex++;
        }

        for (int i = 0; i < playersWithoutBye.size(); i += 2) {
            slots.set(matchIndex * 2, playersWithoutBye.get(i));
            slots.set(matchIndex * 2 + 1, playersWithoutBye.get(i + 1));
            matchIndex++;
        }

        return slots;
    }

    private void validateSeededPlayers(
            List<Player> slots,
            List<Player> firstPlaces,
            List<Player> secondPlaces) {

        List<Long> expectedIds = new ArrayList<>();

        firstPlaces.forEach(player ->
                expectedIds.add(player.getId()));

        secondPlaces.forEach(player ->
                expectedIds.add(player.getId()));

        List<Long> actualIds = slots.stream()
                .filter(player -> player != null)
                .map(Player::getId)
                .toList();

        if (actualIds.size() != expectedIds.size()
                || actualIds.stream().distinct().count() != actualIds.size()
                || !new HashSet<>(actualIds).equals(new HashSet<>(expectedIds))) {

            throw new BusinessException(
                    "O chaveamento gerado não contém exatamente todos os classificados.");
        }
    }

    // =========================================================
    // CRIAÇÃO DA PRIMEIRA FASE
    // =========================================================

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

            if (player1 != null && player2 != null) {

                match.setPlayer1(player1);
                match.setPlayer2(player2);
                match.setStatus(MatchStatus.SCHEDULED);

            } else {

                // O jogador com BYE avança automaticamente.
                match.setPlayer1(player1 != null ? player1 : player2);
                match.setStatus(MatchStatus.FINISHED);
            }

            matches.add(matchRepository.save(match));
        }

        return matches;
    }

    // =========================================================
    // CRIAÇÃO DAS FASES SEGUINTES
    // =========================================================

    private List<Match> createSubsequentRoundMatches(
            Category category,
            int bracketSize) {

        List<Match> matches = new ArrayList<>();

        MatchPhase currentPhase = determineFirstRoundPhase(bracketSize);

        int matchesInRound = bracketSize / 4;

        while (currentPhase != MatchPhase.FINAL) {

            MatchPhase nextPhase = getNextPhase(currentPhase);

            if (nextPhase == null) {
                break;
            }

            for (int i = 0; i < matchesInRound; i++) {

                Match match = new Match();

                match.setCategory(category);
                match.setPhase(nextPhase);
                match.setStatus(MatchStatus.SCHEDULED);

                matches.add(matchRepository.save(match));
            }

            currentPhase = nextPhase;
            matchesInRound /= 2;
        }

        return matches;
    }

    // =========================================================
    // PROGRESSÃO DOS BYES
    // =========================================================

    private void advancePlayersWithBye(
            List<Match> matches,
            int bracketSize) {

        MatchPhase currentPhase = determineFirstRoundPhase(bracketSize);

        MatchPhase nextPhase = getNextPhase(currentPhase);

        if (nextPhase == null) {
            return;
        }

        List<Match> currentRound = getMatchesByPhase(
                matches,
                currentPhase);

        List<Match> nextRound = getMatchesByPhase(
                matches,
                nextPhase);

        /*
         * Duas partidas consecutivas alimentam uma partida da
         * próxima fase:
         *
         * Partidas 0 e 1 -> próxima partida 0.
         * Partidas 2 e 3 -> próxima partida 1.
         * Partidas 4 e 5 -> próxima partida 2.
         *
         * A partida seguinte permanece SCHEDULED enquanto aguarda
         * o vencedor do outro confronto.
         */
        for (int i = 0; i < currentRound.size(); i++) {

            Match currentMatch = currentRound.get(i);

            Player byeWinner = getByeWinner(currentMatch);

            if (byeWinner == null) {
                continue;
            }

            int targetIndex = i / 2;

            if (targetIndex >= nextRound.size()) {
                throw new BusinessException(
                        "Não foi encontrada a partida da próxima fase para o BYE.");
            }

            Match targetMatch = nextRound.get(targetIndex);

            if (i % 2 == 0) {

                if (targetMatch.getPlayer1() != null) {
                    throw new BusinessException(
                            "A vaga do jogador na próxima fase já está ocupada.");
                }

                targetMatch.setPlayer1(byeWinner);

            } else {

                if (targetMatch.getPlayer2() != null) {
                    throw new BusinessException(
                            "A vaga do jogador na próxima fase já está ocupada.");
                }

                targetMatch.setPlayer2(byeWinner);
            }

            // Não finalizar: o jogador ainda precisa disputar
            // a partida da próxima fase contra seu futuro adversário.
            targetMatch.setStatus(MatchStatus.SCHEDULED);

            matchRepository.save(targetMatch);
        }
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

    // =========================================================
    // FASES DO CHAVEAMENTO
    // =========================================================

    private MatchPhase determineFirstRoundPhase(int bracketSize) {

        return switch (bracketSize) {
            case 64 -> MatchPhase.ROUND_OF_64;
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
            case ROUND_OF_64 -> MatchPhase.ROUND_OF_32;
            case ROUND_OF_32 -> MatchPhase.ROUND_OF_16;
            case ROUND_OF_16 -> MatchPhase.QUARTER_FINAL;
            case QUARTER_FINAL -> MatchPhase.SEMI_FINAL;
            case SEMI_FINAL -> MatchPhase.FINAL;
            default -> null;
        };
    }

    private List<Match> getMatchesByPhase(
            List<Match> matches,
            MatchPhase phase) {

        return matches.stream()
                .filter(match -> match.getPhase() == phase)
                .sorted(Comparator.comparing(Match::getId))
                .toList();
    }

    // =========================================================
    // CLASSIFICAÇÃO DOS GRUPOS
    // =========================================================

    public List<Player> getOrderedGroupWinners(Long categoryId) {

        return getPlayersByStandingPosition(categoryId, 0);
    }

    private List<Player> getOrderedGroupSecondPlaces(Long categoryId) {

        return getPlayersByStandingPosition(categoryId, 1);
    }

    public List<Player> getOrderedQualifiedPlayers(Long categoryId) {

        List<Player> result = new ArrayList<>(
                getOrderedGroupWinners(categoryId));

        result.addAll(
                getOrderedGroupSecondPlaces(categoryId));

        return result;
    }

    private List<Player> getPlayersByStandingPosition(
            Long categoryId,
            int position) {

        List<Group> groups = getOrderedGroups(categoryId);

        return getPlayersByStandingPosition(groups, position);
    }

    private List<Player> getPlayersByStandingPosition(
            List<Group> groups,
            int position) {

        List<Player> result = new ArrayList<>();

        for (Group group : groups) {

            List<GroupStandingDTO> standings =
                    groupService.calculateGroupStandings(group.getId());

            if (standings.size() <= position) {
                continue;
            }

            Long playerId = standings.get(position).getPlayerId();

            playerRepository.findById(playerId)
                    .ifPresent(result::add);
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

    // =========================================================
    // REGENERAÇÃO DO CHAVEAMENTO
    // =========================================================

    private void removeExistingKnockoutIfNotStarted(Long categoryId) {

        List<Match> existingMatches = matchRepository
                .findByCategoryId(categoryId)
                .stream()
                .filter(match -> match.getPhase() != MatchPhase.GROUP)
                .toList();

        boolean knockoutStarted = existingMatches.stream()
                .anyMatch(match ->
                        match.getStatus() == MatchStatus.IN_PROGRESS
                        || match.getStatus() == MatchStatus.FINISHED
                        || match.getStatus() == MatchStatus.WO);

        if (knockoutStarted) {
            throw new BusinessException(
                    "Não é possível regenerar o mata-mata porque já existem partidas iniciadas ou encerradas.");
        }

        if (!existingMatches.isEmpty()) {

            matchRepository.deleteAll(existingMatches);
            matchRepository.flush();
        }
    }

    // =========================================================
    // BUSCA DA CATEGORIA
    // =========================================================

    private Category findCategory(Long categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(
                        "Categoria não encontrada com ID: " + categoryId));
    }

    // =========================================================
    // RESPOSTA
    // =========================================================

    private BracketResponseDTO buildBracketResponse(
            Category category,
            int bracketSize,
            List<Match> matches) {

        BracketResponseDTO response = new BracketResponseDTO();

        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setTargetBracketSize(bracketSize);

        response.setMatches(
                matches.stream()
                        .map(this::mapMatchToResponseDTO)
                        .toList());

        return response;
    }

    private MatchResponseDTO mapMatchToResponseDTO(Match match) {

        MatchResponseDTO dto = new MatchResponseDTO();

        dto.setId(match.getId());
        dto.setCategoryId(match.getCategory().getId());
        dto.setCategoryName(match.getCategory().getName());

        if (match.getPlayer1() != null) {
            dto.setPlayer1(
                    toPlayerSummary(match.getPlayer1()));
        }

        if (match.getPlayer2() != null) {
            dto.setPlayer2(
                    toPlayerSummary(match.getPlayer2()));
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
    
    private void validateKnockoutCanBeRegenerated(Long categoryId) {

        boolean knockoutStarted = matchRepository
                .findByCategoryId(categoryId)
                .stream()
                .filter(match -> match.getPhase() != MatchPhase.GROUP)
                .anyMatch(match ->
                        match.getStatus() == MatchStatus.IN_PROGRESS
                        || match.getStatus() == MatchStatus.FINISHED
                        || match.getStatus() == MatchStatus.WO);

        if (knockoutStarted) {
            throw new BusinessException(
                    "Não é possível regenerar o mata-mata porque já existem partidas iniciadas ou encerradas.");
        }
    }
}