package com.arenapointhub.api.dto;

import com.arenapointhub.api.model.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}