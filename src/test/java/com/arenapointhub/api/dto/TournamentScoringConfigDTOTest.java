package com.arenapointhub.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.arenapointhub.api.model.enums.ScoringSystemType;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

class TournamentScoringConfigDTOTest {

    @Test
    void shouldRejectNegativePointsInAllScoringFields() {
        TournamentScoringConfigDTO dto = new TournamentScoringConfigDTO();
        dto.setTournamentId(1L);
        dto.setSystemType(ScoringSystemType.CLASSIC);

        dto.setPointsGroupStageLoser(-1);
        dto.setPointsRoundOf32Loser(-2);
        dto.setPointsRoundOf16Loser(-3);
        dto.setPointsQuarterFinalsLoser(-4);
        dto.setPointsSemiFinalsLoser(-5);
        dto.setPointsRunnerUp(-6);
        dto.setPointsChampion(-7);
        dto.setPoints1stPlace(-8);
        dto.setPoints2ndPlace(-9);
        dto.setPoints3rdPlace(-10);
        dto.setPoints4thPlace(-11);
        dto.setPoints5thPlace(-12);
        dto.setPoints6thPlace(-13);
        dto.setPoints7thPlace(-14);
        dto.setPoints8thPlace(-15);
        dto.setPoints9thAndBeyond(-16);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<TournamentScoringConfigDTO>> violations =
                    validator.validate(dto);

            assertEquals(16, violations.size());
        }
    }
}
