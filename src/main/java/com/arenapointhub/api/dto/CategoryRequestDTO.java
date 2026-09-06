package com.arenapointhub.api.dto;

import com.arenapointhub.api.model.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryRequestDTO {

    @NotBlank(message = "O nome da categoria é obrigatório")
    private String name;

    @NotNull(message = "O tipo da categoria é obrigatório (AGE ou SKILL)")
    private CategoryType type;

    private Integer minAge;
    private Integer maxAge;
    private String description;
    private String scoringSystem;

    @NotNull(message = "O ID do torneio é obrigatório")
    private Long tournamentId;

    public CategoryRequestDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScoringSystem() {
        return scoringSystem;
    }

    public void setScoringSystem(String scoringSystem) {
        this.scoringSystem = scoringSystem;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }
}