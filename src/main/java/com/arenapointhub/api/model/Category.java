package com.arenapointhub.api.model;

import java.util.ArrayList;
import java.util.List;

import com.arenapointhub.api.model.enums.CategoryType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tournament_id", nullable = false)
	private Tournament tournament;

	@Column(nullable = false)
	private String name;

	private String description;

	@Enumerated(EnumType.STRING)
	private CategoryType type;

	@Column(name = "scoring_system")
	private String scoringSystem;

	@Column(name = "min_age")
	private Integer minAge;

	@Column(name = "max_age")
	private Integer maxAge;

	@ManyToMany
	@JoinTable(
		name = "registrations", 
		joinColumns = @JoinColumn(name = "category_id"), 
		inverseJoinColumns = @JoinColumn(name = "player_id")
	)
	private List<Player> players = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	private List<Group> groups = new ArrayList<>();

	public Category() {
	}

	public Category(Long id, Tournament tournament, String name, String description, CategoryType type,
			String scoringSystem, Integer minAge, Integer maxAge, List<Player> players, List<Group> groups) {
		this.id = id;
		this.tournament = tournament;
		this.name = name;
		this.description = description;
		this.type = type;
		this.scoringSystem = scoringSystem;
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.players = players;
		this.groups = groups;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Tournament getTournament() {
		return tournament;
	}

	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CategoryType getType() {
		return type;
	}

	public void setType(CategoryType type) {
		this.type = type;
	}

	public String getScoringSystem() {
		return scoringSystem;
	}

	public void setScoringSystem(String scoringSystem) {
		this.scoringSystem = scoringSystem;
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

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
}