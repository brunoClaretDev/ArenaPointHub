package com.arenapointhub.api.dto;

import jakarta.validation.constraints.NotNull;

public class MatchWoRequestDTO {

    @NotNull(message = "O ID do jogador vencedor por WO é obrigatório.")
    private Long winnerPlayerId;

    public Long getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public void setWinnerPlayerId(Long winnerPlayerId) {
        this.winnerPlayerId = winnerPlayerId;
    }
}