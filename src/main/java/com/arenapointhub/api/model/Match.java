package com.arenapointhub.api.model;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player1_id", nullable = false)
	private Player player1;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player2_id", nullable = false)
	private Player player2;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winner_id")
	private Player winner;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MatchStage stage;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MatchStatus status = MatchStatus.PEDING;
	
	@OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("setNumber ASC")
	private List<SetResult> sets = new ArrayList<>();
	
}

enum MatchStage{
	PREMILINARY_ROUND, 	// Rodada Preliminar / Repescagem
	GROUP,				// Fase de Grupos
	ROUND_OF_64,		// 32 avos de final
	ROUND_OF_32,		// 16 avos de final
	ROUND_OF_16,		// Oitavas de final
	QUARTERFINALS,		// Quartas de final
	SEMIFINALS,			// Semifinais
	THIRD_PLACE,		// Disputa de 3º lugar
	FINAL				// Grande Final
}
enum MatchStatus{
	PEDING, IN_PROGRESS, FINISHED
}
