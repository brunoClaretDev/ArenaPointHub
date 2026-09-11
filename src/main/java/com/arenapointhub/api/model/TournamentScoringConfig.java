package com.arenapointhub.api.model;

import com.arenapointhub.api.model.enums.ScoringSystemType;
import jakarta.persistence.*;

@Entity
@Table(name = "tournament_scoring_config")
public class TournamentScoringConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScoringSystemType systemType = ScoringSystemType.CLASSIC;

    // --- Campos para o modelo CLASSIC ---
    private int pointsGroupStageLoser = 0;
    private int pointsRoundOf32Loser = 0;
    private int pointsRoundOf16Loser = 0;
    private int pointsQuarterFinalsLoser = 0;
    private int pointsSemiFinalsLoser = 0;
    private int pointsRunnerUp = 0;
    private int pointsChampion = 0;

    // --- Campos para o modelo BY_POSITION ---
    private int points1stPlace = 100;
    private int points2ndPlace = 90;
    private int points3rdPlace = 80;
    private int points4thPlace = 70;
    private int points5thPlace = 60;
    private int points6thPlace = 50;
    private int points7thPlace = 40;
    private int points8thPlace = 30;
    private int points9thAndBeyond = 10;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public ScoringSystemType getSystemType() { return systemType; }
    public void setSystemType(ScoringSystemType systemType) { this.systemType = systemType; }

    public int getPointsGroupStageLoser() { return pointsGroupStageLoser; }
    public void setPointsGroupStageLoser(int pointsGroupStageLoser) { this.pointsGroupStageLoser = pointsGroupStageLoser; }

    public int getPointsRoundOf32Loser() { return pointsRoundOf32Loser; }
    public void setPointsRoundOf32Loser(int pointsRoundOf32Loser) { this.pointsRoundOf32Loser = pointsRoundOf32Loser; }

    public int getPointsRoundOf16Loser() { return pointsRoundOf16Loser; }
    public void setPointsRoundOf16Loser(int pointsRoundOf16Loser) { this.pointsRoundOf16Loser = pointsRoundOf16Loser; }

    public int getPointsQuarterFinalsLoser() { return pointsQuarterFinalsLoser; }
    public void setPointsQuarterFinalsLoser(int pointsQuarterFinalsLoser) { this.pointsQuarterFinalsLoser = pointsQuarterFinalsLoser; }

    public int getPointsSemiFinalsLoser() { return pointsSemiFinalsLoser; }
    public void setPointsSemiFinalsLoser(int pointsSemiFinalsLoser) { this.pointsSemiFinalsLoser = pointsSemiFinalsLoser; }

    public int getPointsRunnerUp() { return pointsRunnerUp; }
    public void setPointsRunnerUp(int pointsRunnerUp) { this.pointsRunnerUp = pointsRunnerUp; }

    public int getPointsChampion() { return pointsChampion; }
    public void setPointsChampion(int pointsChampion) { this.pointsChampion = pointsChampion; }

    public int getPoints1stPlace() { return points1stPlace; }
    public void setPoints1stPlace(int points1stPlace) { this.points1stPlace = points1stPlace; }

    public int getPoints2ndPlace() { return points2ndPlace; }
    public void setPoints2ndPlace(int points2ndPlace) { this.points2ndPlace = points2ndPlace; }

    public int getPoints3rdPlace() { return points3rdPlace; }
    public void setPoints3rdPlace(int points3rdPlace) { this.points3rdPlace = points3rdPlace; }

    public int getPoints4thPlace() { return points4thPlace; }
    public void setPoints4thPlace(int points4thPlace) { this.points4thPlace = points4thPlace; }

    public int getPoints5thPlace() { return points5thPlace; }
    public void setPoints5thPlace(int points5thPlace) { this.points5thPlace = points5thPlace; }

    public int getPoints6thPlace() { return points6thPlace; }
    public void setPoints6thPlace(int points6thPlace) { this.points6thPlace = points6thPlace; }

    public int getPoints7thPlace() { return points7thPlace; }
    public void setPoints7thPlace(int points7thPlace) { this.points7thPlace = points7thPlace; }

    public int getPoints8thPlace() { return points8thPlace; }
    public void setPoints8thPlace(int points8thPlace) { this.points8thPlace = points8thPlace; }

    public int getPoints9thAndBeyond() { return points9thAndBeyond; }
    public void setPoints9thAndBeyond(int points9thAndBeyond) { this.points9thAndBeyond = points9thAndBeyond; }
}