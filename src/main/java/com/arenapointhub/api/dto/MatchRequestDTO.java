package com.arenapointhub.api.dto;

import jakarta.validation.constraints.NotNull;

public class MatchRequestDTO {

    private Long groupId; // <-- ADICIONADO

    @NotNull(message = "A categoria é obrigatória.")
    private Long categoryId;

    @NotNull(message = "O jogador 1 é obrigatório.")
    private Long player1Id;

    @NotNull(message = "O jogador 2 é obrigatório.")
    private Long player2Id;

    private String tableOrCourt;
    private String scheduledTime;
    private com.arenapointhub.api.model.enums.MatchStatus status;
    private Integer scorePlayer1;
    private Integer scorePlayer2;

    // Getters e Setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(Long player1Id) {
        this.player1Id = player1Id;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(Long player2Id) {
        this.player2Id = player2Id;
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

    public com.arenapointhub.api.model.enums.MatchStatus getStatus() {
        return status;
    }

    public void setStatus(com.arenapointhub.api.model.enums.MatchStatus status) {
        this.status = status;
    }
}