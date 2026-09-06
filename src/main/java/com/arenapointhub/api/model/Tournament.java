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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

}

enum TournamentStatus {
	UPCOMING, REGISTRARION_OPEN, IN_PROGRESS, FINISHED
}
