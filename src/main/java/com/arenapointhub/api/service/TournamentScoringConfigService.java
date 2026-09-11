package com.arenapointhub.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.TournamentScoringConfigDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.TournamentScoringConfig;
import com.arenapointhub.api.repository.TournamentRepository;
import com.arenapointhub.api.repository.TournamentScoringConfigRepository;

@Service
public class TournamentScoringConfigService {

    private final TournamentScoringConfigRepository configRepository;
    private final TournamentRepository tournamentRepository;

    public TournamentScoringConfigService(TournamentScoringConfigRepository configRepository, 
                                          TournamentRepository tournamentRepository) {
        this.configRepository = configRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional
    public TournamentScoringConfigDTO saveOrUpdateConfig(TournamentScoringConfigDTO dto) {
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new BusinessException("Torneio não encontrado com ID: " + dto.getTournamentId()));

        TournamentScoringConfig config = configRepository.findByTournamentId(dto.getTournamentId())
                .orElse(new TournamentScoringConfig());

        config.setTournament(tournament);
        config.setSystemType(dto.getSystemType());

        // Atribuindo os pontos do modelo Clássico
        config.setPointsGroupStageLoser(dto.getPointsGroupStageLoser());
        config.setPointsRoundOf32Loser(dto.getPointsRoundOf32Loser());
        config.setPointsRoundOf16Loser(dto.getPointsRoundOf16Loser());
        config.setPointsQuarterFinalsLoser(dto.getPointsQuarterFinalsLoser());
        config.setPointsSemiFinalsLoser(dto.getPointsSemiFinalsLoser());
        config.setPointsRunnerUp(dto.getPointsRunnerUp());
        config.setPointsChampion(dto.getPointsChampion());

        // Atribuindo os pontos do modelo Por Colocação
        config.setPoints1stPlace(dto.getPoints1stPlace());
        config.setPoints2ndPlace(dto.getPoints2ndPlace());
        config.setPoints3rdPlace(dto.getPoints3rdPlace());
        config.setPoints4thPlace(dto.getPoints4thPlace());
        config.setPoints5thPlace(dto.getPoints5thPlace());
        config.setPoints6thPlace(dto.getPoints6thPlace());
        config.setPoints7thPlace(dto.getPoints7thPlace());
        config.setPoints8thPlace(dto.getPoints8thPlace());
        config.setPoints9thAndBeyond(dto.getPoints9thAndBeyond());

        TournamentScoringConfig saved = configRepository.save(config);
        
        dto.setId(saved.getId());
        return dto;
    }

    @Transactional(readOnly = true)
    public TournamentScoringConfigDTO getConfigByTournament(Long tournamentId) {
        TournamentScoringConfig config = configRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new BusinessException("Configuração de pontuação não encontrada para este torneio."));

        TournamentScoringConfigDTO dto = new TournamentScoringConfigDTO();
        dto.setId(config.getId());
        dto.setTournamentId(config.getTournament().getId());
        dto.setSystemType(config.getSystemType());

        dto.setPointsGroupStageLoser(config.getPointsGroupStageLoser());
        dto.setPointsRoundOf32Loser(config.getPointsRoundOf32Loser());
        dto.setPointsRoundOf16Loser(config.getPointsRoundOf16Loser());
        dto.setPointsQuarterFinalsLoser(config.getPointsQuarterFinalsLoser());
        dto.setPointsSemiFinalsLoser(config.getPointsSemiFinalsLoser());
        dto.setPointsRunnerUp(config.getPointsRunnerUp());
        dto.setPointsChampion(config.getPointsChampion());

        dto.setPoints1stPlace(config.getPoints1stPlace());
        dto.setPoints2ndPlace(config.getPoints2ndPlace());
        dto.setPoints3rdPlace(config.getPoints3rdPlace());
        dto.setPoints4thPlace(config.getPoints4thPlace());
        dto.setPoints5thPlace(config.getPoints5thPlace());
        dto.setPoints6thPlace(config.getPoints6thPlace());
        dto.setPoints7thPlace(config.getPoints7thPlace());
        dto.setPoints8thPlace(config.getPoints8thPlace());
        dto.setPoints9thAndBeyond(config.getPoints9thAndBeyond());

        return dto;
    }
}