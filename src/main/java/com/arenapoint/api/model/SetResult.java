package com.arenapoint.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "set_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_id", nullable = false)
	private Match match;

	@Column(name = "set_number", nullable = false)
	private Integer setNumber;

	@Column(name = "player1_score", nullable = false)
	private Integer player1Scote;

	@Column(name = "player2_score", nullable = false)
	private Integer player2Scote;

}
