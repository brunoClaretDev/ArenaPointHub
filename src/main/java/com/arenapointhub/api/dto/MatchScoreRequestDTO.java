package com.arenapointhub.api.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class MatchScoreRequestDTO {

    @NotEmpty(message = "É necessário informar ao menos o resultado de um set.")
    @Valid
    private List<MatchSetDTO> sets;

    public List<MatchSetDTO> getSets() {
        return sets;
    }

    public void setSets(List<MatchSetDTO> sets) {
        this.sets = sets;
    }
}