package com.arenapointhub.api.dto;

import com.arenapointhub.api.model.enums.MatchStatus;
import jakarta.validation.constraints.NotNull;

public class MatchScoreUpdateDTO {

    @NotNull(message = "O placar do jogador 1 é obrigatório")
    private Integer scorePlayer1;

    @NotNull(message = "O placar do jogador 2 é obrigatório")
    private Integer scorePlayer2;

    private MatchStatus status;

    public MatchScoreUpdateDTO() {
    }

    public MatchScoreUpdateDTO(Integer scorePlayer1, Integer scorePlayer2, MatchStatus status) {
        this.scorePlayer1 = scorePlayer1;
        this.scorePlayer2 = scorePlayer2;
        this.status = status;
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