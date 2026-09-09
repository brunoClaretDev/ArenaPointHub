package com.arenapointhub.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "match_sets")
public class MatchSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private Integer setNumber; // Ex: 1, 2, 3...

    @Column(nullable = false)
    private Integer scorePlayer1;

    @Column(nullable = false)
    private Integer scorePlayer2;

    public MatchSet() {
    }

    public MatchSet(Match match, Integer setNumber, Integer scorePlayer1, Integer scorePlayer2) {
        this.match = match;
        this.setNumber = setNumber;
        this.scorePlayer1 = scorePlayer1;
        this.scorePlayer2 = scorePlayer2;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public Integer getScorePlayer1() {
        return scorePlayer1;
    }

    public void setScorePlayer1(Integer scorePlayer1) {
        this.scorePlayer1 = scorePlayer1;
    }

    public Integer getScorePlayer2() {
        return scorePlayer2;
    }

    public void setScorePlayer2(Integer scorePlayer2) {
        this.scorePlayer2 = scorePlayer2;
    }
}