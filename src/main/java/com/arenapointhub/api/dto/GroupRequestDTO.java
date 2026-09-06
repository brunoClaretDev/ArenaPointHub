package com.arenapointhub.api.dto;

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
public class GroupRequestDTO {

    @NotBlank(message = "O nome do grupo é obrigatório")
    private String name;

    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoryId;
}