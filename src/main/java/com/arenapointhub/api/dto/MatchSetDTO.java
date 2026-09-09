package com.arenapointhub.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class MatchSetDTO {

    @NotNull(message = "O número do set é obrigatório.")
    private Integer setNumber;

    @NotNull(message = "A pontuação do jogador 1 é obrigatória.")
    @PositiveOrZero(message = "A pontuação não pode ser negativa.")
    private Integer scorePlayer1;

    @NotNull(message = "A pontuação do jogador 2 é obrigatória.")
    @PositiveOrZero(message = "A pontuação não pode ser negativa.")
    private Integer scorePlayer2;

    public MatchSetDTO() {
    }

    public MatchSetDTO(Integer setNumber, Integer scorePlayer1, Integer scorePlayer2) {
        this.setNumber = setNumber;
        this.scorePlayer1 = scorePlayer1;
        this.scorePlayer2 = scorePlayer2;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
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
}