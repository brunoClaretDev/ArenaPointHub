
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

	private static final int MAX_BRACKET_SIZE = 32;

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
    public BracketResponseDTO generateKnockoutBracket(
            Long categoryId,
            Long administratorId) {

        Category category = findCategory(categoryId, administratorId);

        validateKnockoutCanBeRegenerated(categoryId);
        validateGroupMatchesCompleted(categoryId);

        List<Group> groups = getOrderedGroups(
                categoryId, administratorId);

        List<Player> firstPlaces =
                getPlayersByStandingPosition(groups, 0, administratorId);

        List<Player> secondPlaces =
                getPlayersByStandingPosition(groups, 1, administratorId);

        validateQualifiedPlayers(
                groups.size(),
                firstPlaces,
                secondPlaces);

        int totalQualified = firstPlaces.size() + secondPlaces.size();

        int bracketSize = calculateBracketSize(totalQualified);

        List<Player> seededPlayers = buildSeededPlayers(
                firstPlaces,
                secondPlaces,
                bracketSize);

        removeExistingKnockoutIfNotStarted(categoryId);

        List<Match> matches = new ArrayList<>();

        matches.addAll(createFirstRoundMatches(
                category, seededPlayers, bracketSize));

        matches.addAll(createSubsequentRoundMatches(
                category, bracketSize));

        advancePlayersWithBye(matches, bracketSize);

        return buildBracketResponse(category, bracketSize, matches);
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
        
        if (groupCount > 16) {
            throw new BusinessException(
                    "O mata-mata permite no máximo 16 grupos "
                    + "e 32 jogadores classificados.");
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

        if (totalPlayers > MAX_BRACKET_SIZE) {
            throw new BusinessException(
                    "O mata-mata permite no máximo 32 classificados.");
        }

        int size = 2;

        while (size < totalPlayers) {
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
                && bracketSize != 32) {

            throw new BusinessException(
                    "Tamanho de chave inválido para gerar Playoffs.");
        }
    }

    // =========================================================
    // MONTAGEM DO CHAVEAMENTO CONFORME A PLANILHA
    // =========================================================

    private List<Player> buildSeededPlayers(
            List<Player> firstPlaces,
            List<Player> secondPlaces,
            int bracketSize) {

        int groupCount = firstPlaces.size();

        if (groupCount == 1) {
            return new ArrayList<>(List.of(
                    firstPlaces.get(0),
                    secondPlaces.get(0)));
        }

        String[] template = getSeedingTemplate(groupCount);

        if (template.length != bracketSize) {
            throw new BusinessException(
                    "O modelo de chaveamento não corresponde ao tamanho da chave.");
        }

        int expectedByes = calculateRequiredByes(
                firstPlaces.size() + secondPlaces.size(), bracketSize);

        long emptySlots = 0;
        for (String seed : template) {
            if (seed == null) {
                emptySlots++;
            }
        }

        if (emptySlots != expectedByes) {
            throw new BusinessException(
                    "A quantidade de vagas BYE do modelo não corresponde aos classificados.");
        }

        List<Player> slots = new ArrayList<>(template.length);

        for (String seed : template) {
            slots.add(resolveSeededPlayer(seed, firstPlaces, secondPlaces));
        }

        validateSeededPlayers(slots, firstPlaces, secondPlaces);

        return slots;
    }

    /**
     * Modelos de posicionamento extraídos da planilha "Modelos de grupos.xlsx".
     * A posição null representa uma vaga sem participante: BYE (500) ou
     * posição marcada com 0 na planilha, cujo valor deve ser ignorado.
     */
    private String[] getSeedingTemplate(int groupCount) {

        return switch (groupCount) {
            case 2 -> new String[] {
                    "1.1", "2.2", "1.2", "2.1"
                };
            case 3 -> new String[] {
                    "1.1", null, "2.3", "2.2",
                    "1.3", "2.1", "1.2", null
                };
            case 4 -> new String[] {
                    "1.1", "2.3", "1.4", "2.2",
                    "1.3", "2.1", "1.2", "2.4"
                };
            case 5 -> new String[] {
                    "1.1", null, "2.3", "2.5",
                    null, "2.2", "1.4", null,
                    "1.3", null, "1.5", null,
                    "2.1", "2.4", "1.2", null
                };
            case 6 -> new String[] {
                    "1.1", null, "2.3", "2.5",
                    "1.6", "2.2", "1.4", null,
                    "1.3", null, "1.5", "2.1",
                    "2.6", "2.4", "1.2", null
                };
            case 7 -> new String[] {
                    "1.1", null, "2.7", "2.5",
                    "1.6", "2.2", "1.4", "2.3",
                    "1.3", "2.6", "1.5", "2.1",
                    "1.7", "2.4", "1.2", null
                };
            case 8 -> new String[] {
                    "1.1", "2.7", "1.8", "2.5",
                    "1.6", "2.2", "1.4", "2.3",
                    "1.3", "2.6", "1.5", "2.1",
                    "1.7", "2.4", "1.2", "2.8"
                };
            case 9 -> new String[] {
                    "1.1", null, "2.2", null,
                    "2.3", null, "1.8", null,
                    "1.6", null, "2.9", "2.7",
                    "2.5", null, "1.4", null,
                    "1.3", null, null, "2.4",
                    "2.8", "2.6", "1.5", null,
                    "1.7", null, "1.9", null,
                    null, "2.1", "1.2", null
                };
            case 10 -> new String[] {
                    "1.1", null, "2.2", null,
                    "1.10", null, "1.8", null,
                    "1.6", null, "2.9", "2.5",
                    "2.7", "2.3", "1.4", null,
                    "1.3", null, "2.10", "2.4",
                    "2.8", "2.6", "1.5", null,
                    "1.7", null, "1.9", null,
                    null, "2.1", "1.2", null
                };
            case 11 -> new String[] {
                    "1.1", null, "2.2", "2.9",
                    "1.10", null, "1.8", null,
                    "1.6", null, "2.11", "2.5",
                    "2.7", "2.3", "1.4", null,
                    "1.3", null, "2.10", "2.4",
                    "1.11", "2.6", "1.5", null,
                    "1.7", null, "1.9", null,
                    "2.8", "2.1", "1.2", null
                };
            case 12 -> new String[] {
                    "1.1", null, "2.2", "2.9",
                    "1.10", "2.11", "1.8", null,
                    "1.6", null, "1.12", "2.5",
                    "2.7", "2.3", "1.4", null,
                    "1.3", null, "2.10", "2.4",
                    "1.11", "2.6", "1.5", null,
                    "1.7", null, "1.9", "2.8",
                    "2.12", "2.1", "1.2", null
                };
            case 13 -> new String[] {
                    "1.1", null, "2.2", "2.13",
                    "1.10", "2.11", "1.8", "2.9",
                    "1.6", null, "1.12", "2.5",
                    "2.7", "2.3", "1.4", null,
                    "1.3", null, "1.13", "2.4",
                    "1.11", "2.6", "1.5", null,
                    "1.7", "2.10", "1.9", "2.8",
                    "2.12", "2.1", "1.2", null
                };
            case 14 -> new String[] {
                    "1.1", null, "2.2", "2.13",
                    "1.10", "2.11", "1.8", "2.9",
                    "1.6", "2.7", "1.12", "2.5",
                    "1.14", "2.3", "1.4", null,
                    "1.3", null, "1.13", "2.4",
                    "1.11", "2.6", "1.5", "2.12",
                    "1.7", "2.10", "1.9", "2.8",
                    "2.14", "2.1", "1.2", null
                };
            case 15 -> new String[] {
                    "1.1", null, "2.15", "2.13",
                    "1.10", "2.11", "1.8", "2.9",
                    "1.6", "2.7", "1.12", "2.5",
                    "1.14", "2.3", "1.4", "2.2",
                    "1.3", "2.14", "1.13", "2.4",
                    "1.11", "2.6", "1.5", "2.12",
                    "1.7", "2.10", "1.9", "2.8",
                    "1.15", "2.1", "1.2", null
                };
            case 16 -> new String[] {
                    "1.1", "2.15", "1.16", "2.13",
                    "1.10", "2.11", "1.8", "2.9",
                    "1.6", "2.7", "1.12", "2.5",
                    "1.14", "2.3", "1.4", "2.2",
                    "1.3", "2.14", "1.13", "2.4",
                    "1.11", "2.6", "1.5", "2.12",
                    "1.7", "2.10", "1.9", "2.8",
                    "1.15", "2.1", "1.2", "2.16"
                };
            default -> throw new BusinessException(
                    "Não existe modelo de chaveamento para essa quantidade de grupos.");
        };
    }

    private Player resolveSeededPlayer(
            String seed,
            List<Player> firstPlaces,
            List<Player> secondPlaces) {

        if (seed == null) {
            return null;
        }

        List<Player> source;
        if (seed.startsWith("1.")) {
            source = firstPlaces;
        } else if (seed.startsWith("2.")) {
            source = secondPlaces;
        } else {
            throw new BusinessException(
                    "Identificação inválida no modelo de chaveamento: " + seed);
        }

        int groupNumber;
        try {
            groupNumber = Integer.parseInt(seed.substring(2));
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    "Identificação inválida no modelo de chaveamento: " + seed);
        }

        int playerIndex = groupNumber - 1;
        if (playerIndex < 0 || playerIndex >= source.size()) {
            throw new BusinessException(
                    "O modelo de chaveamento referencia um grupo inexistente: " + seed);
        }

        return source.get(playerIndex);
    }

    private void validateSeededPlayers(
            List<Player> slots,
            List<Player> firstPlaces,
            List<Player> secondPlaces) {

        List<Long> expectedIds = new ArrayList<>();

        firstPlaces.forEach(player -> expectedIds.add(player.getId()));
        secondPlaces.forEach(player -> expectedIds.add(player.getId()));

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
    // CRIAÇÃO DAS PARTIDAS
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
                match.setPlayer1(player1 != null ? player1 : player2);
                match.setStatus(MatchStatus.FINISHED);
            }

            matches.add(matchRepository.save(match));
        }

        return matches;
    }

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

        List<Match> currentRound = getMatchesByPhase(matches, currentPhase);
        List<Match> nextRound = getMatchesByPhase(matches, nextPhase);

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

    public List<Player> getOrderedGroupWinners(
            Long categoryId,
            Long administratorId) {

        return getPlayersByStandingPosition(
                categoryId, 0, administratorId);
    }

    private List<Player> getOrderedGroupSecondPlaces(
            Long categoryId,
            Long administratorId) {

        return getPlayersByStandingPosition(
                categoryId, 1, administratorId);
    }

    public List<Player> getOrderedQualifiedPlayers(
            Long categoryId,
            Long administratorId) {

        List<Player> result = new ArrayList<>(
                getOrderedGroupWinners(categoryId, administratorId));

        result.addAll(
                getOrderedGroupSecondPlaces(categoryId, administratorId));

        return result;
    }

    private List<Player> getPlayersByStandingPosition(
            Long categoryId,
            int position,
            Long administratorId) {

        List<Group> groups = getOrderedGroups(
                categoryId, administratorId);

        return getPlayersByStandingPosition(
                groups, position, administratorId);
    }

    private List<Player> getPlayersByStandingPosition(
            List<Group> groups,
            int position,
            Long administratorId) {

        List<Player> result = new ArrayList<>();

        for (Group group : groups) {
            List<GroupStandingDTO> standings =
                    groupService.calculateGroupStandings(
                            group.getId(), administratorId);

            if (standings.size() <= position) {
                continue;
            }

            Long playerId = standings.get(position).getPlayerId();

            playerRepository.findById(playerId)
                    .ifPresent(result::add);
        }

        return result;
    }

    private List<Group> getOrderedGroups(
            Long categoryId,
            Long administratorId) {

        List<Group> groups = new ArrayList<>(
                groupRepository
                        .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                                categoryId, administratorId));

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

    // =========================================================
    // VALIDAÇÃO DAS PARTIDAS DE GRUPO
    // =========================================================

    private void validateGroupMatchesCompleted(Long categoryId) {

        boolean hasGroupMatches = matchRepository
                .existsByCategoryIdAndPhase(
                        categoryId, MatchPhase.GROUP);

        if (!hasGroupMatches) {
            throw new BusinessException(
                    "Não existem partidas de grupo cadastradas para esta categoria.");
        }

        boolean hasPendingMatches = matchRepository
                .existsByCategoryIdAndPhaseAndStatusNotIn(
                        categoryId,
                        MatchPhase.GROUP,
                        List.of(MatchStatus.FINISHED, MatchStatus.WO));

        if (hasPendingMatches) {
            throw new BusinessException(
                    "Não é possível gerar o mata-mata enquanto existirem partidas de grupo pendentes ou canceladas.");
        }
    }

    // =========================================================
    // BUSCA DA CATEGORIA
    // =========================================================

    private Category findCategory(
            Long categoryId,
            Long administratorId) {

        return categoryRepository
                .findByIdAndTournament_Administrator_Id(
                        categoryId, administratorId)
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
