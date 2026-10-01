package com.arenapointhub.api.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "player_global_ranking",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ranking_player_category_year",
            columnNames = {"player_id", "category_id", "ranking_year"}
        )
    }
)
public class PlayerGlobalRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "ranking_year", nullable = false)
    private int year;

    private int totalPoints = 0;

    private int tournamentsPlayed = 0;

    public PlayerGlobalRanking() {
    }

    public PlayerGlobalRanking(
            Player player,
            Category category,
            int year,
            int totalPoints,
            int tournamentsPlayed) {
        this.player = player;
        this.category = category;
        this.year = year;
        this.totalPoints = totalPoints;
        this.tournamentsPlayed = tournamentsPlayed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getTournamentsPlayed() {
        return tournamentsPlayed;
    }

    public void setTournamentsPlayed(int tournamentsPlayed) {
        this.tournamentsPlayed = tournamentsPlayed;
    }
}