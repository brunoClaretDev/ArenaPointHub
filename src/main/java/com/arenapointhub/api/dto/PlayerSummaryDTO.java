package com.arenapointhub.api.dto;

public class PlayerSummaryDTO {

    private Long id;
    private String name;
    private String clubAcademy;

    public PlayerSummaryDTO() {
    }

    public PlayerSummaryDTO(Long id, String name, String clubAcademy) {
        this.id = id;
        this.name = name;
        this.clubAcademy = clubAcademy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClubAcademy() {
        return clubAcademy;
    }

    public void setClubAcademy(String clubAcademy) {
        this.clubAcademy = clubAcademy;
    }
}