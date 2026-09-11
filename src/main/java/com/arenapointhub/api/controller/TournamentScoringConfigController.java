package com.arenapointhub.api.controller;

import com.arenapointhub.api.dto.TournamentScoringConfigDTO;
import com.arenapointhub.api.service.TournamentScoringConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments/scoring-config")
public class TournamentScoringConfigController {

    private final TournamentScoringConfigService configService;

    public TournamentScoringConfigController(TournamentScoringConfigService configService) {
        this.configService = configService;
    }

    @PostMapping
    public ResponseEntity<TournamentScoringConfigDTO> saveOrUpdateConfig(@Valid @RequestBody TournamentScoringConfigDTO dto) {
        TournamentScoringConfigDTO savedConfig = configService.saveOrUpdateConfig(dto);
        return ResponseEntity.ok(savedConfig);
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<TournamentScoringConfigDTO> getConfigByTournament(@PathVariable Long tournamentId) {
        TournamentScoringConfigDTO config = configService.getConfigByTournament(tournamentId);
        return ResponseEntity.ok(config);
    }
}