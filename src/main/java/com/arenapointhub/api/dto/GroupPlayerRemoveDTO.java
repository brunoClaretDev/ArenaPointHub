package com.arenapointhub.api.dto;

import jakarta.validation.constraints.NotNull;

public class GroupPlayerRemoveDTO {

    @NotNull(message = "O ID do jogador é obrigatório.")
    private Long playerId;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
}