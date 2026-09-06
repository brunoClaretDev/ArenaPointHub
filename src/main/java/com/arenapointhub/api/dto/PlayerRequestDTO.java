package com.arenapointhub.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class PlayerRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    private String email;

    @NotNull(message = "A data de nascimento é obrigatória")
    private LocalDate birthDate;

    private String phone;
    private String clubAcademy;

    public PlayerRequestDTO() {
    }

    public PlayerRequestDTO(String name, String email, LocalDate birthDate, String phone, String clubAcademy) {
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.phone = phone;
        this.clubAcademy = clubAcademy;
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