package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.TournamentScoringConfigDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.TournamentScoringConfig;
import com.arenapointhub.api.model.enums.ScoringSystemType;
import com.arenapointhub.api.repository.TournamentRepository;
import com.arenapointhub.api.repository.TournamentScoringConfigRepository;

class TournamentScoringConfigServiceTest {

    @Mock
    private TournamentScoringConfigRepository configRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    private TournamentScoringConfigService configService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        configService = new TournamentScoringConfigService(
                configRepository,
                tournamentRepository
        );
    }

    @Test
    void shouldCreateScoringConfig() {
        Tournament tournament = createTournament(1L);

        TournamentScoringConfigDTO dto = createConfigDTO();

        TournamentScoringConfig savedConfig = new TournamentScoringConfig();
        savedConfig.setId(10L);

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.of(tournament));

        when(configRepository.findByTournamentId(1L))
                .thenReturn(Optional.empty());

        when(configRepository.save(any(TournamentScoringConfig.class)))
                .thenReturn(savedConfig);

        TournamentScoringConfigDTO response =
                configService.saveOrUpdateConfig(dto);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getTournamentId());
        assertEquals(
                ScoringSystemType.CLASSIC,
                response.getSystemType()
        );
        assertEquals(10, response.getPointsChampion());
        assertEquals(5, response.getPointsRunnerUp());

        verify(tournamentRepository).findById(1L);
        verify(configRepository).findByTournamentId(1L);
        verify(configRepository).save(any(TournamentScoringConfig.class));
    }

    @Test
    void shouldUpdateExistingScoringConfig() {
        Tournament tournament = createTournament(1L);

        TournamentScoringConfig existingConfig =
                createScoringConfig(20L, tournament);

        TournamentScoringConfigDTO dto = createConfigDTO();
        dto.setPointsChampion(20);

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.of(tournament));

        when(configRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(existingConfig));

        when(configRepository.save(any(TournamentScoringConfig.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TournamentScoringConfigDTO response =
                configService.saveOrUpdateConfig(dto);

        assertEquals(20L, response.getId());
        assertEquals(20, response.getPointsChampion());

        assertEquals(20, existingConfig.getPointsChampion());

        verify(configRepository).save(existingConfig);
    }

    @Test
    void shouldPersistAllAdministratorConfiguredPointValues() {
        Tournament tournament = createTournament(1L);
        TournamentScoringConfigDTO dto = createConfigDTO();
        dto.setPointsGroupStageLoser(11);
        dto.setPointsRoundOf32Loser(12);
        dto.setPointsRoundOf16Loser(13);
        dto.setPointsQuarterFinalsLoser(14);
        dto.setPointsSemiFinalsLoser(15);
        dto.setPointsRunnerUp(16);
        dto.setPointsChampion(17);
        dto.setPoints1stPlace(21);
        dto.setPoints2ndPlace(22);
        dto.setPoints3rdPlace(23);
        dto.setPoints4thPlace(24);
        dto.setPoints5thPlace(25);
        dto.setPoints6thPlace(26);
        dto.setPoints7thPlace(27);
        dto.setPoints8thPlace(28);
        dto.setPoints9thAndBeyond(29);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(configRepository.findByTournamentId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any(TournamentScoringConfig.class)))
                .thenAnswer(invocation -> {
                    TournamentScoringConfig saved = invocation.getArgument(0);
                    saved.setId(30L);
                    return saved;
                });

        configService.saveOrUpdateConfig(dto);

        ArgumentCaptor<TournamentScoringConfig> captor =
                ArgumentCaptor.forClass(TournamentScoringConfig.class);
        verify(configRepository).save(captor.capture());
        TournamentScoringConfig saved = captor.getValue();

        assertEquals(ScoringSystemType.CLASSIC, saved.getSystemType());
        assertEquals(11, saved.getPointsGroupStageLoser());
        assertEquals(12, saved.getPointsRoundOf32Loser());
        assertEquals(13, saved.getPointsRoundOf16Loser());
        assertEquals(14, saved.getPointsQuarterFinalsLoser());
        assertEquals(15, saved.getPointsSemiFinalsLoser());
        assertEquals(16, saved.getPointsRunnerUp());
        assertEquals(17, saved.getPointsChampion());
        assertEquals(21, saved.getPoints1stPlace());
        assertEquals(22, saved.getPoints2ndPlace());
        assertEquals(23, saved.getPoints3rdPlace());
        assertEquals(24, saved.getPoints4thPlace());
        assertEquals(25, saved.getPoints5thPlace());
        assertEquals(26, saved.getPoints6thPlace());
        assertEquals(27, saved.getPoints7thPlace());
        assertEquals(28, saved.getPoints8thPlace());
        assertEquals(29, saved.getPoints9thAndBeyond());
    }

    @Test
    void shouldReturnScoringConfigByTournament() {
        Tournament tournament = createTournament(1L);

        TournamentScoringConfig config =
                createScoringConfig(10L, tournament);

        when(configRepository.findByTournamentId(1L))
                .thenReturn(Optional.of(config));

        TournamentScoringConfigDTO response =
                configService.getConfigByTournament(1L);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getTournamentId());
        assertEquals(
                ScoringSystemType.CLASSIC,
                response.getSystemType()
        );
        assertEquals(10, response.getPointsChampion());
        assertEquals(5, response.getPointsRunnerUp());
        assertEquals(3, response.getPointsSemiFinalsLoser());
        assertEquals(2, response.getPointsQuarterFinalsLoser());
        assertEquals(1, response.getPointsGroupStageLoser());
        assertEquals(2, response.getPointsRoundOf32Loser());
        assertEquals(3, response.getPointsRoundOf16Loser());
        assertEquals(10, response.getPoints1stPlace());
        assertEquals(5, response.getPoints2ndPlace());
        assertEquals(3, response.getPoints3rdPlace());
        assertEquals(2, response.getPoints4thPlace());
        assertEquals(1, response.getPoints5thPlace());
        assertEquals(1, response.getPoints6thPlace());
        assertEquals(1, response.getPoints7thPlace());
        assertEquals(1, response.getPoints8thPlace());
        assertEquals(0, response.getPoints9thAndBeyond());

        verify(configRepository).findByTournamentId(1L);
    }

    @Test
    void shouldNotReturnScoringConfigWhenItDoesNotExist() {
        when(configRepository.findByTournamentId(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> configService.getConfigByTournament(1L)
        );

        assertEquals(
                "Configuração de pontuação não encontrada para este torneio.",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotSaveScoringConfigWhenTournamentDoesNotExist() {
        TournamentScoringConfigDTO dto = createConfigDTO();

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> configService.saveOrUpdateConfig(dto)
        );

        assertEquals(
                "Torneio não encontrado com ID: 1",
                exception.getMessage()
        );

        verify(tournamentRepository).findById(1L);
    }

    private TournamentScoringConfigDTO createConfigDTO() {
        TournamentScoringConfigDTO dto =
                new TournamentScoringConfigDTO();

        dto.setTournamentId(1L);
        dto.setSystemType(ScoringSystemType.CLASSIC);

        dto.setPointsGroupStageLoser(1);
        dto.setPointsRoundOf32Loser(2);
        dto.setPointsRoundOf16Loser(3);
        dto.setPointsQuarterFinalsLoser(2);
        dto.setPointsSemiFinalsLoser(3);
        dto.setPointsRunnerUp(5);
        dto.setPointsChampion(10);

        dto.setPoints1stPlace(10);
        dto.setPoints2ndPlace(5);
        dto.setPoints3rdPlace(3);
        dto.setPoints4thPlace(2);
        dto.setPoints5thPlace(1);
        dto.setPoints6thPlace(1);
        dto.setPoints7thPlace(1);
        dto.setPoints8thPlace(1);
        dto.setPoints9thAndBeyond(0);

        return dto;
    }

    private Tournament createTournament(Long id) {
        Tournament tournament = new Tournament();
        tournament.setId(id);
        tournament.setName("Tournament Test");
        return tournament;
    }

    private TournamentScoringConfig createScoringConfig(
            Long id,
            Tournament tournament) {

        TournamentScoringConfig config =
                new TournamentScoringConfig();

        config.setId(id);
        config.setTournament(tournament);
        config.setSystemType(ScoringSystemType.CLASSIC);

        config.setPointsGroupStageLoser(1);
        config.setPointsRoundOf32Loser(2);
        config.setPointsRoundOf16Loser(3);
        config.setPointsQuarterFinalsLoser(2);
        config.setPointsSemiFinalsLoser(3);
        config.setPointsRunnerUp(5);
        config.setPointsChampion(10);

        config.setPoints1stPlace(10);
        config.setPoints2ndPlace(5);
        config.setPoints3rdPlace(3);
        config.setPoints4thPlace(2);
        config.setPoints5thPlace(1);
        config.setPoints6thPlace(1);
        config.setPoints7thPlace(1);
        config.setPoints8thPlace(1);
        config.setPoints9thAndBeyond(0);

        return config;
    }
}