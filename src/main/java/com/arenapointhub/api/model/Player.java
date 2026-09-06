package com.arenapointhub.api.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDate birthDate;

    private String phone;
    private String clubAcademy;

    // Construtor sem argumentos (Obrigatório para o JPA)
    public Player() {
    }

    // Construtor completo
    public Player(Long id, String name, String email, LocalDate birthDate, String phone, String clubAcademy) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.phone = phone;
        this.clubAcademy = clubAcademy;
    }

    // --- GETTERS E SETTERS MANUAIS ---

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