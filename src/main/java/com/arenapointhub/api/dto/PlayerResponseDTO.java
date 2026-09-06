package com.arenapointhub.api.dto;

import java.time.LocalDate;

public class PlayerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private LocalDate birthDate;
    private String phone;
    private String clubAcademy;

    // Construtor padrão (sem argumentos)
    public PlayerResponseDTO() {
    }

    // Construtor completo com todos os parâmetros
    public PlayerResponseDTO(Long id, String name, String email, LocalDate birthDate, String phone, String clubAcademy) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.phone = phone;
        this.clubAcademy = clubAcademy;
    }

    // Getters e Setters explicito
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClubAcademy() {
        return clubAcademy;
    }

    public void setClubAcademy(String clubAcademy) {
        this.clubAcademy = clubAcademy;
    }
}