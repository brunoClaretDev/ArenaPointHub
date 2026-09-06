package com.arenapointhub.api.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tournaments")
public class Tournament {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String schedule;
	private String location;

	@Enumerated(EnumType.STRING)
	private TournamentStatus status = TournamentStatus.UPCOMING;

	@OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Category> categories = new ArrayList<>();

	public Tournament() {
	}

	public Tournament(Long id, String name, String description, String schedule, String location,
			TournamentStatus status, List<Category> categories) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.schedule = schedule;
		this.location = location;
		this.status = status;
		this.categories = categories;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public TournamentStatus getStatus() {
		return status;
	}

	public void setStatus(TournamentStatus status) {
		this.status = status;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
}

enum TournamentStatus {
	UPCOMING, REGISTRARION_OPEN, IN_PROGRESS, FINISHED
}