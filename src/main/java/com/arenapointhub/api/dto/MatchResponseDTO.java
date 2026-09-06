package com.arenapointhub.api.dto;

import com.arenapointhub.api.model.enums.MatchStatus;

public class MatchResponseDTO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private PlayerSummaryDTO player1;
    private PlayerSummaryDTO player2;
    private String tableOrCourt;
    private String scheduledTime;
    private Integer scorePlayer1;
    private Integer scorePlayer2;
    private MatchStatus status;

    public MatchResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public PlayerSummaryDTO getPlayer1() {
        return player1;
    }

    public void setPlayer1(PlayerSummaryDTO player1) {
        this.player1 = player1;
    }

    public PlayerSummaryDTO getPlayer2() {
        return player2;
    }

    public void setPlayer2(PlayerSummaryDTO player2) {
        this.player2 = player2;
    }

    public String getTableOrCourt() {
        return tableOrCourt;
    }

    public void setTableOrCourt(String tableOrCourt) {
        this.tableOrCourt = tableOrCourt;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Integer getScorePlayer1() {
        return scorePlayer1;
    }

    public void setScorePlayer1(Integer scorePlayer1) {
        this.scorePlayer1 = scorePlayer1;
    }

    public Integer getScorePlayer2() {
        return scorePlayer2;
    }

    public void setScorePlayer2(Integer scorePlayer2) {
        this.scorePlayer2 = scorePlayer2;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }
}