package com.arenapointhub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSummaryDTO {

    private Long id;
    private String name;
    private String clubAcademy;
}