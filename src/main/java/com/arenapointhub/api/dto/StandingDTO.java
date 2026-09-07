package com.arenapointhub.api.dto;

public class StandingDTO implements Comparable<StandingDTO> {

    private Long playerId;
    private String playerName;
    private String clubAcademy;
    private int played;
    private int wins;
    private int losses;
    private int setsWon;
    private int setsLost;
    private int points;

    public StandingDTO() {
    }

    public StandingDTO(Long playerId, String playerName, String clubAcademy, int played, int wins, int losses, int setsWon, int setsLost, int points) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.clubAcademy = clubAcademy;
        this.played = played;
        this.wins = wins;
        this.losses = losses;
        this.setsWon = setsWon;
        this.setsLost = setsLost;
        this.points = points;
    }

    public int getSetBalance() {
        return setsWon - setsLost;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getClubAcademy() {
        return clubAcademy;
    }

    public void setClubAcademy(String clubAcademy) {
        this.clubAcademy = clubAcademy;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getSetsWon() {
        return setsWon;
    }

    public void setSetsWon(int setsWon) {
        this.setsWon = setsWon;
    }

    public int getSetsLost() {
        return setsLost;
    }

    public void setSetsLost(int setsLost) {
        this.setsLost = setsLost;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public int compareTo(StandingDTO other) {
        if (this.points != other.points) {
            return Integer.compare(other.points, this.points);
        }
        if (this.wins != other.wins) {
            return Integer.compare(other.wins, this.wins);
        }
        if (this.getSetBalance() != other.getSetBalance()) {
            return Integer.compare(other.getSetBalance(), this.getSetBalance());
        }
        return Integer.compare(other.setsWon, this.setsWon);
    }
}