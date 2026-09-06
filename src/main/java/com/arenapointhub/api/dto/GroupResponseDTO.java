package com.arenapointhub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDTO {

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private List<PlayerSummaryDTO> players;
}