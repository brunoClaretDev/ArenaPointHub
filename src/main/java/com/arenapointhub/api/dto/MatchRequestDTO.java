package com.arenapointhub.api.dto;

import com.arenapointhub.api.model.enums.MatchStatus;
import jakarta.validation.constraints.NotNull;

public class MatchRequestDTO {

    @NotNull(message = "A categoria é obrigatória")
    private Long categoryId;

    @NotNull(message = "O Jogador 1 é obrigatório")
    private Long player1Id;

    @NotNull(message = "O Jogador 2 é obrigatório")
    private Long player2Id;

    private String tableOrCourt;
    private String scheduledTime;
    private Integer scorePlayer1;
    private Integer scorePlayer2;
    private MatchStatus status;

    public MatchRequestDTO() {
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