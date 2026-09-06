package com.arenapointhub.api.dto;

import com.arenapointhub.api.model.enums.TournamentStatus;

import jakarta.validation.constraints.NotBlank;

public class TournamentRequestDTO {

    @NotBlank(message = "O nome do torneio é obrigatório")
    private String name;

    private String description;
    private String schedule;
    private String location;
    private TournamentStatus status;

    public TournamentRequestDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }
}