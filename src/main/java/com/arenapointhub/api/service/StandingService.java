package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.StandingDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;

@Service
public class StandingService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public List<StandingDTO> calculateGroupStandings(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado com ID: " + groupId));

        Map<Long, StandingDTO> standingsMap = new HashMap<>();

        // Inicializa a tabela com todos os jogadores do grupo com 0 pontos/estatísticas
        for (Player player : group.getPlayers()) {
            standingsMap.put(player.getId(), new StandingDTO(
                player.getId(),
                player.getName(),
                player.getClubAcademy(),
                0, 0, 0, 0, 0, 0
            ));
        }

        // Busca todas as partidas da categoria vinculada ao grupo
        // (Ou filtra por partidas cujos jogadores pertençam ao grupo)
        List<Match> matches = matchRepository.findByCategoryId(group.getCategory().getId());

        for (Match match : matches) {
            // Considera apenas partidas finalizadas e que envolvem jogadores deste grupo
            if (match.getStatus() == MatchStatus.FINISHED) {
                Long p1Id = match.getPlayer1().getId();
                Long p2Id = match.getPlayer2().getId();

                if (standingsMap.containsKey(p1Id) && standingsMap.containsKey(p2Id)) {
                    StandingDTO s1 = standingsMap.get(p1Id);
                    StandingDTO s2 = standingsMap.get(p2Id);

                    s1.setPlayed(s1.getPlayed() + 1);
                    s2.setPlayed(s2.getPlayed() + 1);

                    int score1 = match.getScorePlayer1() != null ? match.getScorePlayer1() : 0;
                    int score2 = match.getScorePlayer2() != null ? match.getScorePlayer2() : 0;

                    s1.setSetsWon(s1.getSetsWon() + score1);
                    s1.setSetsLost(s1.getSetsLost() + score2);
                    s2.setSetsWon(s2.getSetsWon() + score2);
                    s2.setSetsLost(s2.getSetsLost() + score1);

                    if (score1 > score2) {
                        s1.setWins(s1.getWins() + 1);
                        s1.setPoints(s1.getPoints() + 2); // Ex: 2 pontos por vitória
                        s2.setLosses(s2.getLosses() + 1);
                        s2.setPoints(s2.getPoints() + 1); // Ex: 1 ponto por derrota
                    } else if (score2 > score1) {
                        s2.setWins(s2.getWins() + 1);
                        s2.setPoints(s2.getPoints() + 2);
                        s1.setLosses(s1.getLosses() + 1);
                        s1.setPoints(s1.getPoints() + 1);
                    }
                }
            }
        }

        List<StandingDTO> standingsList = new ArrayList<>(standingsMap.values());
        Collections.sort(standingsList); // Ordena de acordo com os critérios definidos no DTO
        return standingsList;
    }
}