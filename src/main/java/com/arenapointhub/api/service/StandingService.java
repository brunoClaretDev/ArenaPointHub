package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.StandingDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;

@Service
public class StandingService {

	private final GroupRepository groupRepository;
	private final MatchRepository matchRepository;

	public StandingService(GroupRepository groupRepository, MatchRepository matchRepository) {
		this.groupRepository = groupRepository;
		this.matchRepository = matchRepository;
	}

	@Transactional(readOnly = true)
	public List<StandingDTO> calculateGroupStandings(Long groupId) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new BusinessException("Grupo não encontrado com ID: " + groupId));

		Map<Long, StandingDTO> standingsMap = new HashMap<>();

		// Inicializa a tabela com todos os jogadores do grupo com 0 pontos/estatísticas
		for (Player player : group.getPlayers()) {
			standingsMap.put(player.getId(),
					new StandingDTO(player.getId(), player.getName(), player.getClubAcademy(), 0, 0, 0, 0, 0, 0));
		}

		// Busca todas as partidas da categoria vinculada ao grupo
		// (Ou filtra por partidas cujos jogadores pertençam ao grupo)
		List<Match> matches = matchRepository.findByCategoryId(group.getCategory().getId());

		for (Match match : matches) {
			if (match.getPhase() != MatchPhase.GROUP
					|| (match.getStatus() != MatchStatus.FINISHED && match.getStatus() != MatchStatus.WO)
					|| match.getPlayer1() == null || match.getPlayer2() == null) {
				continue;
			}

			Long player1Id = match.getPlayer1().getId();
			Long player2Id = match.getPlayer2().getId();

			if (!standingsMap.containsKey(player1Id) || !standingsMap.containsKey(player2Id)) {
				continue;
			}

			StandingDTO standing1 = standingsMap.get(player1Id);
			StandingDTO standing2 = standingsMap.get(player2Id);

			int score1 = match.getScorePlayer1() != null ? match.getScorePlayer1() : 0;
			int score2 = match.getScorePlayer2() != null ? match.getScorePlayer2() : 0;

			standing1.setPlayed(standing1.getPlayed() + 1);
			standing2.setPlayed(standing2.getPlayed() + 1);

			standing1.setSetsWon(standing1.getSetsWon() + score1);
			standing1.setSetsLost(standing1.getSetsLost() + score2);
			standing2.setSetsWon(standing2.getSetsWon() + score2);
			standing2.setSetsLost(standing2.getSetsLost() + score1);

			if (score1 > score2) {
				standing1.setWins(standing1.getWins() + 1);
				standing1.setPoints(standing1.getPoints() + 3);
				standing2.setLosses(standing2.getLosses() + 1);
				standing2.setPoints(standing2.getPoints() + 1);
			} else if (score2 > score1) {
				standing2.setWins(standing2.getWins() + 1);
				standing2.setPoints(standing2.getPoints() + 3);
				standing1.setLosses(standing1.getLosses() + 1);
				standing1.setPoints(standing1.getPoints() + 1);
			}
		}

		List<StandingDTO> standingsList = new ArrayList<>(standingsMap.values());
		Collections.sort(standingsList); // Ordena de acordo com os critérios definidos no DTO
		return standingsList;
	}
}