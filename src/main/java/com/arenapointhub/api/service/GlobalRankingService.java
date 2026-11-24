package com.arenapointhub.api.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
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
    public void distributeTournamentPoints(
            Tournament tournament,
            Category category,
            Map<Player, Integer> playerPositions) {

    	if (category.isRankingProcessed()) {
    	    return;
    	}

    	if (playerPositions == null || playerPositions.isEmpty()) {
    	    return;
    	}
    	
        TournamentScoringConfig config =
                scoringConfigRepository.findByTournamentId(tournament.getId())
                        .orElseThrow(() -> new BusinessException(
                                "Configuração de pontuação não encontrada para este torneio."));

        int currentYear = LocalDate.now().getYear();

        for (Map.Entry<Player, Integer> entry : playerPositions.entrySet()) {
            Player player = entry.getKey();
            int position = entry.getValue();

            int pointsEarned = calculatePointsForPosition(config, position);

            PlayerGlobalRanking globalRanking = rankingRepository
                    .findByPlayerIdAndCategoryIdAndYear(
                            player.getId(),
                            category.getId(),
                            currentYear)
                    .orElseGet(() -> {
                        PlayerGlobalRanking newRanking = new PlayerGlobalRanking();
                        newRanking.setPlayer(player);
                        newRanking.setCategory(category);
                        newRanking.setYear(currentYear);
                        newRanking.setTotalPoints(0);
                        newRanking.setTournamentsPlayed(0);
                        return newRanking;
                    });

            globalRanking.setTotalPoints(
                    globalRanking.getTotalPoints() + pointsEarned);

            globalRanking.setTournamentsPlayed(
                    globalRanking.getTournamentsPlayed() + 1);

            rankingRepository.save(globalRanking);
        }
        
        category.setRankingProcessed(true);
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
                case -1: return config.getPointsGroupStageLoser();
                case 1: return config.getPointsChampion();
                case 2: return config.getPointsRunnerUp();
                case 3:
                case 4: return config.getPointsSemiFinalsLoser();
                case 5: return config.getPointsQuarterFinalsLoser();
                case 9: return config.getPointsRoundOf16Loser();
                case 17: return config.getPointsRoundOf32Loser();
                default: return config.getPointsGroupStageLoser();
            }
        }
    }
    
    @Transactional
    public void processTournamentCompletion(
            Tournament tournament,
            List<Match> matches) {

        Category category = matches.stream()
                .map(Match::getCategory)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Não foi possível identificar a categoria do torneio."));

        Map<Player, Integer> playerPositions =
                determinePlayerPositions(matches);

        distributeTournamentPoints(tournament, category, playerPositions);
    }

    private Map<Player, Integer> determinePlayerPositions(List<Match> matches) {
        Map<Player, Integer> positions = new HashMap<>();

        Match finalMatch = matches.stream()
                .filter(match -> match.getPhase() == MatchPhase.FINAL
                        && (match.getStatus() == MatchStatus.FINISHED
                                || match.getStatus() == MatchStatus.WO))
                .findFirst()
                .orElse(null);

        if (finalMatch == null
                || finalMatch.getPlayer1() == null
                || finalMatch.getPlayer2() == null
                || finalMatch.getScorePlayer1() == finalMatch.getScorePlayer2()) {
            return positions;
        }

        Player champion = finalMatch.getScorePlayer1() > finalMatch.getScorePlayer2()
                ? finalMatch.getPlayer1()
                : finalMatch.getPlayer2();
        Player runnerUp = champion.equals(finalMatch.getPlayer1())
                ? finalMatch.getPlayer2()
                : finalMatch.getPlayer1();

        positions.put(champion, 1);
        positions.put(runnerUp, 2);

        // A semifinalist's position is determined by the finalist they lost to:
        // the loser of the champion's semifinal is 3rd; the loser of the runner-up's
        // semifinal is 4th. This follows the bracket path, not match-list ordering.
        for (Match match : matches) {
            if (match.getPhase() != MatchPhase.SEMI_FINAL
                    || !isCompleted(match)
                    || match.getPlayer1() == null
                    || match.getPlayer2() == null) {
                continue;
            }

            Player semifinalWinner = getWinner(match);
            Player semifinalLoser = getLoser(match);
            if (semifinalWinner == null || semifinalLoser == null) {
                continue;
            }

            if (semifinalWinner.equals(champion)) {
                positions.put(semifinalLoser, 3);
            } else if (semifinalWinner.equals(runnerUp)) {
                positions.put(semifinalLoser, 4);
            }
        }

        for (Match match : matches) {
            if (!isCompleted(match)) {
                continue;
            }

            if (match.getPhase() == null
                    || match.getPhase() == MatchPhase.FINAL
                    || match.getPhase() == MatchPhase.GROUP
                    || match.getPhase() == MatchPhase.SEMI_FINAL
                    || match.getPlayer1() == null
                    || match.getPlayer2() == null) {
                continue;
            }

            Integer officialPosition = getOfficialPositionForEliminationPhase(match.getPhase());
            if (officialPosition == null) {
                continue;
            }

            Player loser = getLoser(match);
            if (loser != null) {
                positions.put(loser, officialPosition);
            }
        }

        // Usa -1 como marcador interno para eliminados na fase de grupos.
        // Assim, no sistema CLASSIC são consultados os pontos específicos da fase
        // de grupos; no sistema BY_POSITION, o marcador cai na faixa 9+.
        // putIfAbsent evita sobrescrever a colocação de quem avançou no mata-mata.
        for (Match match : matches) {
            if (match.getPhase() != MatchPhase.GROUP
                    || (match.getStatus() != MatchStatus.FINISHED
                            && match.getStatus() != MatchStatus.WO)
                    || match.getPlayer1() == null
                    || match.getPlayer2() == null) {
                continue;
            }

            Player loser = getLoser(match);
            if (loser != null) {
                positions.putIfAbsent(loser, -1);
            }
        }

        return positions;
    }

    /**
     * Retorna a colocação oficial compartilhada por todos os atletas
     * eliminados na mesma fase, conforme a regra de classificação do torneio.
     */
    private Integer getOfficialPositionForEliminationPhase(MatchPhase phase) {
        switch (phase) {
            case QUARTER_FINAL:
                return 5;
            case ROUND_OF_16:
                return 9;
            case ROUND_OF_32:
                return 17;
            case GROUP:
                return -1;
            default:
                return null;
        }
    }

    private boolean isCompleted(Match match) {
        return match.getStatus() == MatchStatus.FINISHED
                || match.getStatus() == MatchStatus.WO;
    }

    private Player getWinner(Match match) {
        if (match.getScorePlayer1() > match.getScorePlayer2()) {
            return match.getPlayer1();
        }
        if (match.getScorePlayer2() > match.getScorePlayer1()) {
            return match.getPlayer2();
        }
        return null;
    }

    private Player getLoser(Match match) {
        if (match.getScorePlayer1() > match.getScorePlayer2()) {
            return match.getPlayer2();
        }
        if (match.getScorePlayer2() > match.getScorePlayer1()) {
            return match.getPlayer1();
        }
        return null;
    }

    
}
