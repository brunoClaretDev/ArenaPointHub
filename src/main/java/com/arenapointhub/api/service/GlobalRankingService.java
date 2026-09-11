package com.arenapointhub.api.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.PlayerGlobalRanking;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.TournamentScoringConfig;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.model.enums.ScoringSystemType;
import com.arenapointhub.api.repository.PlayerGlobalRankingRepository;
import com.arenapointhub.api.repository.TournamentScoringConfigRepository;

@Service
public class GlobalRankingService {

    private final PlayerGlobalRankingRepository rankingRepository;
    private final TournamentScoringConfigRepository scoringConfigRepository;

    public GlobalRankingService(PlayerGlobalRankingRepository rankingRepository,
                                TournamentScoringConfigRepository scoringConfigRepository) {
        this.rankingRepository = rankingRepository;
        this.scoringConfigRepository = scoringConfigRepository;
    }

    @Transactional
    public void distributeTournamentPoints(Tournament tournament, Map<Player, Integer> playerPositions) {
        TournamentScoringConfig config = scoringConfigRepository.findByTournamentId(tournament.getId())
                .orElseThrow(() -> new BusinessException("Configuração de pontuação não encontrada para este torneio."));

        int currentYear = LocalDate.now().getYear();

        for (Map.Entry<Player, Integer> entry : playerPositions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue(); // 1 para Campeão, 2 para Vice, etc.

            int pointsEarned = calculatePointsForPosition(config, position);

            PlayerGlobalRanking globalRanking = rankingRepository.findByPlayerIdAndYear(player.getId(), currentYear)
                    .orElseGet(() -> {
                        PlayerGlobalRanking newRanking = new PlayerGlobalRanking();
                        newRanking.setPlayer(player);
                        newRanking.setYear(currentYear);
                        newRanking.setTotalPoints(0);
                        newRanking.setTournamentsPlayed(0);
                        return newRanking;
                    });

            globalRanking.setTotalPoints(globalRanking.getTotalPoints() + pointsEarned);
            globalRanking.setTournamentsPlayed(globalRanking.getTournamentsPlayed() + 1);

            rankingRepository.save(globalRanking);
        }
    }

    private int calculatePointsForPosition(TournamentScoringConfig config, int position) {
        if (config.getSystemType() == ScoringSystemType.BY_POSITION) {
            switch (position) {
                case 1: return config.getPoints1stPlace();
                case 2: return config.getPoints2ndPlace();
                case 3: return config.getPoints3rdPlace();
                case 4: return config.getPoints4thPlace();
                case 5: return config.getPoints5thPlace();
                case 6: return config.getPoints6thPlace();
                case 7: return config.getPoints7thPlace();
                case 8: return config.getPoints8thPlace();
                default: return config.getPoints9thAndBeyond();
            }
        } else {
            // Modelo CLASSIC: Mapeia as posições detalhadas para as regras clássicas de perdedores por fase
            switch (position) {
                case 1: return config.getPointsChampion();
                case 2: return config.getPointsRunnerUp();
                case 3:
                case 4: return config.getPointsSemiFinalsLoser();       // 3º e 4º ganham pontos de semifinalista
                case 5:
                case 6:
                case 7:
                case 8: return config.getPointsQuarterFinalsLoser();  // 5º ao 8º ganham pontos de perdedor de quartas
                default: return config.getPointsGroupStageLoser();    // 9+ ganham pontos de fase de grupos
            }
        }
    }
    
    @Transactional
    public void processTournamentCompletion(Tournament tournament, List<Match> matches) {
        Map<Player, Integer> playerPositions = determinePlayerPositions(matches);
        distributeTournamentPoints(tournament, playerPositions);
    }

    private Map<Player, Integer> determinePlayerPositions(List<Match> matches) {
        Map<Player, Integer> positions = new HashMap<>();
        
        // 1. Identifica a partida da Final para descobrir o Campeão e o Vice
        Match finalMatch = matches.stream()
                .filter(m -> m.getPhase() == MatchPhase.FINAL && (m.getStatus() == MatchStatus.FINISHED || m.getStatus() == MatchStatus.WO))
                .findFirst()
                .orElse(null);

        if (finalMatch == null || finalMatch.getPlayer1() == null || finalMatch.getPlayer2() == null) {
            return positions;
        }

        Player p1 = finalMatch.getPlayer1();
        Player p2 = finalMatch.getPlayer2();
        int score1 = finalMatch.getScorePlayer1();
        int score2 = finalMatch.getScorePlayer2();

        Player champion = score1 > score2 ? p1 : p2;
        Player runnerUp = score1 > score2 ? p2 : p1;

        positions.put(champion, 1);
        positions.put(runnerUp, 2);

        // 2. Mapeia as posições hierárquicas das Fases Finais (Semis e Quartas)
        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.FINISHED && match.getStatus() != MatchStatus.WO) continue;
            
            Player mp1 = match.getPlayer1();
            Player mp2 = match.getPlayer2();
            if (mp1 == null || mp2 == null) continue;

            MatchPhase phase = match.getPhase();

            if (phase == MatchPhase.SEMI_FINAL) {
                if (mp1.equals(champion) || mp2.equals(champion)) {
                    Player loser = mp1.equals(champion) ? mp2 : mp1;
                    positions.put(loser, 3); // 3º: Perdeu para o Campeão na Semi
                } else if (mp1.equals(runnerUp) || mp2.equals(runnerUp)) {
                    Player loser = mp1.equals(runnerUp) ? mp2 : mp1;
                    positions.put(loser, 4); // 4º: Perdeu para o Vice na Semi
                }
            } else if (phase == MatchPhase.QUARTER_FINAL) {
                if (mp1.equals(champion) || mp2.equals(champion)) {
                    Player loser = mp1.equals(champion) ? mp2 : mp1;
                    positions.put(loser, 5); // 5º: Perdeu para o Campeão nas Quartas
                } else if (mp1.equals(runnerUp) || mp2.equals(runnerUp)) {
                    Player loser = mp1.equals(runnerUp) ? mp2 : mp1;
                    positions.put(loser, 6); // 6º: Perdeu para o Vice nas Quartas
                } else {
                    // 7º e 8º: Perdedores das quartas que caíram para outros (pontuação inferior aos 5º/6º)
                    Player loser = (match.getScorePlayer1() > match.getScorePlayer2()) ? mp2 : mp1;
                    positions.putIfAbsent(loser, 7); 
                }
            }
        }

     // 3. Mapeia os eliminados na Fase de Grupos para a posição 9+
        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.FINISHED && match.getStatus() != MatchStatus.WO) continue;
            
            if (match.getPhase() == MatchPhase.GROUP) {
                Player mp1 = match.getPlayer1();
                Player mp2 = match.getPlayer2();
                
                if (mp1 != null && mp2 != null) {
                    Player loserGroup = (match.getScorePlayer1() > match.getScorePlayer2()) ? mp2 : mp1;
                    positions.putIfAbsent(loserGroup, 9);
                }
            }
        }
        
        return positions; // <--- Adicione esta linha aqui
    }
    
}
