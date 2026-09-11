package com.arenapointhub.api.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.MatchScoreRequestDTO;
import com.arenapointhub.api.dto.MatchScoreUpdateDTO;
import com.arenapointhub.api.dto.MatchSetDTO;
import com.arenapointhub.api.dto.MatchWoRequestDTO;
import com.arenapointhub.api.dto.PlayerSummaryDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.MatchSet;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.MatchSetRepository;
import com.arenapointhub.api.repository.PlayerRepository;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final CategoryRepository categoryRepository;
    private final PlayerRepository playerRepository;
    private final MatchSetRepository matchSetRepository;
    private final GroupRepository groupRepository;

    public MatchService(MatchRepository matchRepository, 
                        CategoryRepository categoryRepository, 
                        PlayerRepository playerRepository, 
                        MatchSetRepository matchSetRepository,
                        GroupRepository groupRepository) {
        this.matchRepository = matchRepository;
        this.categoryRepository = categoryRepository;
        this.playerRepository = playerRepository;
        this.matchSetRepository = matchSetRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public MatchResponseDTO createMatch(MatchRequestDTO dto) {
        if (dto.getPlayer1Id().equals(dto.getPlayer2Id())) {
            throw new BusinessException("Um jogador não pode jogar contra ele mesmo.");
        }

        boolean courtOccupied = matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                dto.getTableOrCourt(), dto.getScheduledTime(), MatchStatus.CANCELED);
        if (courtOccupied) {
            throw new BusinessException("A mesa/quadra '" + dto.getTableOrCourt() + "' já está ocupada no horário " + dto.getScheduledTime());
        }

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

        if (dto.getGroupId() != null) {
            Group group = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new BusinessException("Grupo não encontrado: " + dto.getGroupId()));
            match.setGroup(group);
        }

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
        
        if (updatedMatch.getPhase() == MatchPhase.FINAL && updatedMatch.getStatus() == MatchStatus.FINISHED) {
            Player champion = (updatedMatch.getScorePlayer1() > updatedMatch.getScorePlayer2()) 
                    ? updatedMatch.getPlayer1() 
                    : (updatedMatch.getScorePlayer2() > updatedMatch.getScorePlayer1() ? updatedMatch.getPlayer2() : null);

            if (champion != null) {
                Category category = updatedMatch.getCategory();
                category.setChampion(champion);
                categoryRepository.save(category);
            }
        }

        // Dispara o avanço caso a partida tenha sido finalizada
        if (updatedMatch.getStatus() == MatchStatus.FINISHED) {
            advanceWinnerToNextRound(updatedMatch);
        }

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
        dto.setPhase(entity.getPhase());
        return dto;
    }
    
    @Transactional(readOnly = true)
    public MatchResponseDTO getMatchById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Partida não encontrada com ID: " + id));
        return mapToResponseDTO(match);
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
        
        if (updatedMatch.getStatus() == MatchStatus.FINISHED) {
            advanceWinnerToNextRound(updatedMatch);
        }

        return mapToResponseDTO(updatedMatch);
    }

    @Transactional
    public void saveMatchSets(Long matchId, MatchScoreRequestDTO dto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("Partida não encontrada com ID: " + matchId));

        for (MatchSetDTO setDto : dto.getSets()) {
            int p1Score = setDto.getScorePlayer1();
            int p2Score = setDto.getScorePlayer2();

            boolean isP1WinnerSet = false;
            boolean isP2WinnerSet = false;

            // Regra padrão: alguém fez 11 e o oponente tem no máximo 9 (ex: 11x0 até 11x9)
            if ((p1Score == 11 && p2Score <= 9) || (p2Score == 11 && p1Score <= 9)) {
                if (p1Score == 11) isP1WinnerSet = true;
                else isP2WinnerSet = true;
            } 
            // Regra de vantagem (Deuce): Ambos chegaram a 10 ou mais, e a diferença é exatamente de 2 pontos
            else if (p1Score >= 10 && p2Score >= 10) {
                int diff = Math.abs(p1Score - p2Score);
                if (diff == 2) {
                    if (p1Score > p2Score) isP1WinnerSet = true;
                    else isP2WinnerSet = true;
                }
            }

            if (!isP1WinnerSet && !isP2WinnerSet) {
                throw new BusinessException("Placar inválido para o set " + setDto.getSetNumber() + 
                    ": O set deve terminar em 11 (com no máximo 9 para o perdedor) ou com 2 pontos de diferença a partir de 10x10 (ex: 12x10, 13x11).");
            }

            MatchSet matchSet = matchSetRepository.findByMatchIdAndSetNumber(matchId, setDto.getSetNumber())
                    .orElse(new MatchSet());

            matchSet.setMatch(match);
            matchSet.setSetNumber(setDto.getSetNumber());
            matchSet.setScorePlayer1(p1Score);
            matchSet.setScorePlayer2(p2Score);

            matchSetRepository.save(matchSet);
        }

        // Recalcula o total de sets ganhos por cada jogador com base nos sets salvos
        List<MatchSet> allSets = matchSetRepository.findByMatchId(matchId);
        int setsWonPlayer1 = 0;
        int setsWonPlayer2 = 0;

        for (MatchSet s : allSets) {
            if (s.getScorePlayer1() > s.getScorePlayer2()) {
                setsWonPlayer1++;
            } else if (s.getScorePlayer2() > s.getScorePlayer1()) {
                setsWonPlayer2++;
            }
        }

        match.setScorePlayer1(setsWonPlayer1);
        match.setScorePlayer2(setsWonPlayer2);

        // Pega quantos sets são necessários para vencer com base na configuração da categoria (Global para todas as fases)
        int setsNeededToWin = determineSetsNeededToWin(match);

        if (setsWonPlayer1 >= setsNeededToWin || setsWonPlayer2 >= setsNeededToWin) {
            match.setStatus(MatchStatus.FINISHED);
            
            Match savedMatch = matchRepository.save(match);

            // Dispara o avanço para a próxima fase apenas quando fechar o confronto
            advanceWinnerToNextRound(savedMatch);
        } else {
            match.setStatus(MatchStatus.IN_PROGRESS);
            matchRepository.save(match);
        }
    }

    // Método auxiliar para buscar a regra global da categoria
    private int determineSetsNeededToWin(Match match) {
        if (match.getCategory() != null && match.getCategory().getSetsToWinMatch() > 0) {
            return match.getCategory().getSetsToWinMatch();
        }
        return 2; // Padrão de segurança: 2 vitórias (melhor de 3)
    }

    @Transactional(readOnly = true)
    public List<MatchSetDTO> getSetsByMatchId(Long matchId) {
        return matchSetRepository.findByMatchId(matchId).stream()
                .map(set -> new MatchSetDTO(set.getSetNumber(), set.getScorePlayer1(), set.getScorePlayer2()))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MatchResponseDTO registerWalkover(Long matchId, MatchWoRequestDTO dto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("Partida não encontrada com ID: " + matchId));

        Long winnerId = dto.getWinnerPlayerId();
        boolean isPlayer1Winner = winnerId.equals(match.getPlayer1().getId());
        boolean isPlayer2Winner = winnerId.equals(match.getPlayer2().getId());

        if (!isPlayer1Winner && !isPlayer2Winner) {
            throw new BusinessException("O jogador informado não faz parte desta partida.");
        }

        match.setStatus(MatchStatus.WO);

        if (isPlayer1Winner) {
            match.setScorePlayer1(2);
            match.setScorePlayer2(0);
        } else {
            match.setScorePlayer1(0);
            match.setScorePlayer2(2);
        }

        Match updatedMatch = matchRepository.save(match);

        // Dispara o avanço por WO também se desejado
        advanceWinnerToNextRound(updatedMatch);

        return mapToResponseDTO(updatedMatch);
    }
    
    private void advanceWinnerToNextRound(Match finishedMatch) {
        if (finishedMatch.getPhase() == null || finishedMatch.getPhase() == MatchPhase.GROUP) {
            return;
        }

        Player winner = (finishedMatch.getScorePlayer1() > finishedMatch.getScorePlayer2()) 
                ? finishedMatch.getPlayer1() 
                : (finishedMatch.getScorePlayer2() > finishedMatch.getScorePlayer1() ? finishedMatch.getPlayer2() : null);

        if (winner == null) {
            return; 
        }

        MatchPhase nextPhase = getNextPhase(finishedMatch.getPhase());
        if (nextPhase == null) {
            return; 
        }

        // Busca todas as partidas da fase atual ordenadas por ID para manter a consistência da árvore
        List<Match> currentPhaseMatches = matchRepository.findByCategoryId(finishedMatch.getCategory().getId())
                .stream()
                .filter(m -> m.getPhase() == finishedMatch.getPhase())
                .sorted(Comparator.comparing(Match::getId))
                .collect(Collectors.toList());

        List<Match> nextRoundMatches = matchRepository.findByCategoryId(finishedMatch.getCategory().getId())
                .stream()
                .filter(m -> m.getPhase() == nextPhase)
                .sorted(Comparator.comparing(Match::getId))
                .collect(Collectors.toList());

        if (nextRoundMatches.isEmpty()) {
            return;
        }

        int matchIndex = -1;
        for (int i = 0; i < currentPhaseMatches.size(); i++) {
            if (currentPhaseMatches.get(i).getId().equals(finishedMatch.getId())) {
                matchIndex = i;
                break;
            }
        }

        if (matchIndex == -1) return;

        // Mapeamento exato das quartas (0, 1, 2, 3) para as semifinais (0 e 1):
        // Quartas 0 e 1 -> Semifinal 0
        // Quartas 2 e 3 -> Semifinal 1
        int targetMatchIndex = matchIndex / 2;
        if (targetMatchIndex >= nextRoundMatches.size()) {
            return;
        }

        Match targetMatch = nextRoundMatches.get(targetMatchIndex);

        // Evita duplicar o mesmo jogador na mesma partida
        boolean isPlayer1ThisWinner = targetMatch.getPlayer1() != null && targetMatch.getPlayer1().getId().equals(winner.getId());
        boolean isPlayer2ThisWinner = targetMatch.getPlayer2() != null && targetMatch.getPlayer2().getId().equals(winner.getId());

        if (isPlayer1ThisWinner || isPlayer2ThisWinner) {
            return;
        }

        // Posiciona o vencedor: indices pares (0 e 2) vão para o player1; ímpares (1 e 3) vão para o player2
        if (matchIndex % 2 == 0) {
            if (targetMatch.getPlayer1() == null) {
                targetMatch.setPlayer1(winner);
                matchRepository.save(targetMatch);
            }
        } else {
            if (targetMatch.getPlayer2() == null) {
                targetMatch.setPlayer2(winner);
                matchRepository.save(targetMatch);
            }
        }
    }

    private com.arenapointhub.api.model.enums.MatchPhase getNextPhase(com.arenapointhub.api.model.enums.MatchPhase currentPhase) {
        switch (currentPhase) {
            case ROUND_OF_32: return com.arenapointhub.api.model.enums.MatchPhase.ROUND_OF_16;
            case ROUND_OF_16: return com.arenapointhub.api.model.enums.MatchPhase.QUARTER_FINAL;
            case QUARTER_FINAL: return com.arenapointhub.api.model.enums.MatchPhase.SEMI_FINAL;
            case SEMI_FINAL: return com.arenapointhub.api.model.enums.MatchPhase.FINAL;
            default: return null;
        }
    }
}