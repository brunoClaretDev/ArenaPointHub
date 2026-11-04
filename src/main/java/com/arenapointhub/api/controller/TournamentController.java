package com.arenapointhub.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.TournamentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    public ResponseEntity<TournamentResponseDTO> createTournament(
            @Valid @RequestBody TournamentRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        TournamentResponseDTO created =
                tournamentService.createTournament(dto, principal.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TournamentResponseDTO>> getAllTournaments(
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                tournamentService.getAllTournaments(principal.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> getTournamentById(
            @PathVariable Long id,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                tournamentService.getTournamentById(id, principal.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> updateTournament(
            @PathVariable Long id,
            @Valid @RequestBody TournamentRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                tournamentService.updateTournament(id, dto, principal.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(
            @PathVariable Long id,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        tournamentService.deleteTournament(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate-playoffs")
    public ResponseEntity<List<BracketResponseDTO>> generatePlayoffs(
            @PathVariable Long id,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                tournamentService.generatePlayoffs(id, principal.id()));
    }
}