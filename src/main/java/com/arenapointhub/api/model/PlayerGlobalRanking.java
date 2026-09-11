package com.arenapointhub.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "player_global_ranking")
public class PlayerGlobalRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "`year`", nullable = false)
    private int year; // Ex: 2026

    private int totalPoints = 0;
    private int tournamentsPlayed = 0;

    // Construtores
    public PlayerGlobalRanking() {}

    public PlayerGlobalRanking(Player player, int year, int totalPoints, int tournamentsPlayed) {
        this.player = player;
        this.year = year;
        this.totalPoints = totalPoints;
        this.tournamentsPlayed = tournamentsPlayed;
    }

    // Getters e Setters manuais
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getTournamentsPlayed() { return tournamentsPlayed; }
    public void setTournamentsPlayed(int tournamentsPlayed) { this.tournamentsPlayed = tournamentsPlayed; }
}