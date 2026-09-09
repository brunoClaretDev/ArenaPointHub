package com.arenapointhub.api.dto;

public class GroupStandingDTO {
    private Long playerId;
    private String playerName;
    private String clubAcademy;
    private int matchesPlayed;
    private int matchesWon;
    private int matchesLost;
    private int setsWon;
    private int setsLost;
    private int setDifference;
    private int points; // Ex: 2 pontos por vitória, 1 por derrota, etc.

    // Getters, Setters e Construtores
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getClubAcademy() { return clubAcademy; }
    public void setClubAcademy(String clubAcademy) { this.clubAcademy = clubAcademy; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }
    public int getMatchesWon() { return matchesWon; }
    public void setMatchesWon(int matchesWon) { this.matchesWon = matchesWon; }
    public int getMatchesLost() { return matchesLost; }
    public void setMatchesLost(int matchesLost) { this.matchesLost = matchesLost; }
    public int getSetsWon() { return setsWon; }
    public void setSetsWon(int setsWon) { this.setsWon = setsWon; }
    public int getSetsLost() { return setsLost; }
    public void setSetsLost(int setsLost) { this.setsLost = setsLost; }
    public int getSetDifference() { return setDifference; }
    public void setSetDifference(int setDifference) { this.setDifference = setDifference; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}