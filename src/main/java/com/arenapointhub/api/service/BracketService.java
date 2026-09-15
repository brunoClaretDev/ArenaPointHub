package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	public BracketService(GroupRepository groupRepository, GroupService groupService, MatchRepository matchRepository,
			PlayerRepository playerRepository, CategoryRepository categoryRepository) {
		this.groupRepository = groupRepository;
		this.groupService = groupService;
		this.matchRepository = matchRepository;
		this.playerRepository = playerRepository;
		this.categoryRepository = categoryRepository;
	}

	/**
	 * Identifica os 1ºs colocados de cada grupo em ordem de criação/nome do grupo.
	 */
	public List<Player> getOrderedGroupWinners(Long categoryId) {
		List<Group> groups = groupRepository.findByCategoryId(categoryId);
		if (groups.isEmpty()) {
			throw new BusinessException("Não existem grupos para esta categoria.");
		}

		// Ordena os grupos alfabeticamente ou por ID (Ex: Grupo A, Grupo B, Grupo 1,
		// Grupo 2...)
		groups.sort((g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName()));

		List<Player> groupWinners = new ArrayList<>();

		for (Group group : groups) {
			List<GroupStandingDTO> standings = groupService.calculateGroupStandings(group.getId());
			if (!standings.isEmpty()) {
				Long topPlayerId = standings.get(0).getPlayerId();
				playerRepository.findById(topPlayerId).ifPresent(groupWinners::add);
			}
		}

		return groupWinners;
	}

	/**
	 * Calcula quantos Byes são necessários com base no total de classificados e no
	 * tamanho alvo da chave (ex: 16).
	 */
	public int calculateRequiredByes(int totalQualifiedPlayers, int targetBracketSize) {
		if (totalQualifiedPlayers > targetBracketSize) {
			throw new BusinessException("O número de classificados não pode ser maior que o tamanho da chave.");
		}
		return targetBracketSize - totalQualifiedPlayers;
	}

	/**
	 * Retorna a lista de jogadores que recebem Bye ordenados pelos 1ºs lugares de
	 * cada grupo.
	 */
	public List<Player> determinePlayersWithBye(Long categoryId, int totalQualifiedPlayers, int targetBracketSize) {
		List<Player> groupWinnersOrdered = getOrderedGroupWinners(categoryId);
		int numberOfByes = calculateRequiredByes(totalQualifiedPlayers, targetBracketSize);

		if (numberOfByes > groupWinnersOrdered.size()) {
			throw new BusinessException(
					"O número de Byes necessários é maior que a quantidade de vencedores de grupo disponíveis.");
		}

		return groupWinnersOrdered.stream().limit(numberOfByes).collect(Collectors.toList());
	}

	@Transactional
	public BracketResponseDTO generateKnockoutBracket(Long categoryId, int targetBracketSize) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new BusinessException("Categoria não encontrada com ID: " + categoryId));

		List<Player> orderedPlayers = getOrderedQualifiedPlayers(categoryId);
		int totalQualified = orderedPlayers.size();

		List<Player> playersWithBye = determinePlayersWithBye(categoryId, totalQualified, targetBracketSize);

		List<Match> createdMatches = createFirstRoundKnockoutMatches(categoryId, orderedPlayers, playersWithBye,
				targetBracketSize);

		BracketResponseDTO responseDTO = new BracketResponseDTO();
		responseDTO.setCategoryId(category.getId());
		responseDTO.setCategoryName(category.getName());
		responseDTO.setTargetBracketSize(targetBracketSize);

		List<MatchResponseDTO> matchDTOs = createdMatches.stream().map(this::mapMatchToResponseDTO)
				.collect(Collectors.toList());

		responseDTO.setMatches(matchDTOs);

		return responseDTO;
	}

	public List<Player> getOrderedQualifiedPlayers(Long categoryId) {
		List<Group> groups = groupRepository.findByCategoryId(categoryId);
		if (groups.isEmpty()) {
			throw new BusinessException("Não existem grupos para esta categoria.");
		}

		// Ordena os grupos alfabeticamente (Grupo A, Grupo B...)
		groups.sort((g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName()));

		List<Player> firstPlacePlayers = new ArrayList<>();
		List<Player> secondPlacePlayers = new ArrayList<>();

		for (Group group : groups) {
			List<GroupStandingDTO> standings = groupService.calculateGroupStandings(group.getId());
			if (standings.size() > 0) {
				playerRepository.findById(standings.get(0).getPlayerId()).ifPresent(firstPlacePlayers::add);
			}
			if (standings.size() > 1) {
				playerRepository.findById(standings.get(1).getPlayerId()).ifPresent(secondPlacePlayers::add);
			}
		}

		// Junta primeiro os 1ºs colocados de cada grupo, seguido dos 2ºs colocados
		List<Player> orderedPlayers = new ArrayList<>(firstPlacePlayers);
		orderedPlayers.addAll(secondPlacePlayers);
		return orderedPlayers;
	}

	private List<Match> createFirstRoundKnockoutMatches(Long categoryId, List<Player> orderedPlayers,
			List<Player> playersWithBye, int bracketSize) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new BusinessException("Categoria não encontrada."));

		List<Match> createdMatches = new ArrayList<>();
		int numMatches = bracketSize / 2;

		for (int i = 0; i < numMatches; i++) {
			Match match = new Match();
			match.setCategory(category);
			match.setStatus(MatchStatus.SCHEDULED);

			Player player1 = i < orderedPlayers.size() ? orderedPlayers.get(i) : null;
			Player player2 = (bracketSize - 1 - i) < orderedPlayers.size() ? orderedPlayers.get(bracketSize - 1 - i)
					: null;

			if (player1 != null && playersWithBye.contains(player1)) {
				match.setPlayer1(player1);
				match.setPlayer2(null); // Bye
				match.setStatus(MatchStatus.FINISHED);
			} else {
				match.setPlayer1(player1);
				match.setPlayer2(player2);
			}

			Match savedMatch = matchRepository.save(match);
			createdMatches.add(savedMatch);
		}

		return createdMatches;
	}

	private MatchResponseDTO mapMatchToResponseDTO(Match match) {
		MatchResponseDTO dto = new MatchResponseDTO();
		dto.setId(match.getId());
		if (match.getCategory() != null) {
			dto.setCategoryId(match.getCategory().getId());
			dto.setCategoryName(match.getCategory().getName());
		}
		if (match.getPlayer1() != null) {
			PlayerSummaryDTO p1Summary = new PlayerSummaryDTO(match.getPlayer1().getId(), match.getPlayer1().getName(),
					match.getPlayer1().getClubAcademy());
			dto.setPlayer1(p1Summary);
		}
		if (match.getPlayer2() != null) {
			PlayerSummaryDTO p2Summary = new PlayerSummaryDTO(match.getPlayer2().getId(), match.getPlayer2().getName(),
					match.getPlayer2().getClubAcademy());
			dto.setPlayer2(p2Summary);
		}
		dto.setScorePlayer1(match.getScorePlayer1());
		dto.setScorePlayer2(match.getScorePlayer2());
		dto.setStatus(match.getStatus());
		dto.setTableOrCourt(match.getTableOrCourt());
		dto.setScheduledTime(match.getScheduledTime());
		return dto;
	}
}