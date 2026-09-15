package com.arenapointhub.api.dto;

import java.util.List;

import com.arenapointhub.api.model.enums.CategoryType;

public class CategoryResponseDTO {

	private Long id;
	private String name;
	private CategoryType type;
	private Integer minAge;
	private Integer maxAge;
	private String description;
	private String scoringSystem;
	private Long tournamentId;
	private int setsToWinMatch;
	private List<PlayerSummaryDTO> players;

	public CategoryResponseDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CategoryType getType() {
		return type;
	}

	public void setType(CategoryType type) {
		this.type = type;
	}

	public Integer getMinAge() {
		return minAge;
	}

	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getScoringSystem() {
		return scoringSystem;
	}

	public void setScoringSystem(String scoringSystem) {
		this.scoringSystem = scoringSystem;
	}

	public Long getTournamentId() {
		return tournamentId;
	}

	public void setTournamentId(Long tournamentId) {
		this.tournamentId = tournamentId;
	}

	public int getSetsToWinMatch() {
		return setsToWinMatch;
	}

	public void setSetsToWinMatch(int setsToWinMatch) {
		this.setsToWinMatch = setsToWinMatch;
	}

	public List<PlayerSummaryDTO> getPlayers() {
		return players;
	}

	public void setPlayers(List<PlayerSummaryDTO> players) {
		this.players = players;
	}
}