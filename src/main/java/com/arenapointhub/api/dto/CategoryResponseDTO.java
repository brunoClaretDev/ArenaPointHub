package com.arenapointhub.api.dto;

import com.arenapointhub.api.model.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private CategoryType type;
    private Integer minAge;
    private Integer maxAge;
    private String description;
    private String scoringSystem;
    private Long tournamentId;
}