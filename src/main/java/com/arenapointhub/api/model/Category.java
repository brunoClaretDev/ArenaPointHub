package com.arenapointhub.api.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
	
	
	
	
	
}
