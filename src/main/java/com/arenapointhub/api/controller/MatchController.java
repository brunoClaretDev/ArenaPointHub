
package com.arenapointhub.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.MatchScoreRequestDTO;
import com.arenapointhub.api.dto.MatchScoreUpdateDTO;
import com.arenapointhub.api.dto.MatchSetDTO;
import com.arenapointhub.api.dto.MatchWoRequestDTO;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.MatchService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchResponseDTO> createMatch(
            @Valid @RequestBody MatchRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        MatchResponseDTO created = matchService.createMatch(
                dto,
                principal.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<MatchResponseDTO>> getAllMatches(
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                matchService.getAllMatches(principal.id()));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesByCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                matchService.getMatchesByCategory(
                        categoryId,
                        principal.id()));
    }

    @PatchMapping("/{id}/score")
    public ResponseEntity<MatchResponseDTO> updateScore(
            @PathVariable Long id,
            @Valid @RequestBody MatchScoreUpdateDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        MatchResponseDTO updated = matchService.updateScore(
                id,
                dto,
                principal.id());

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesByStatus(
            @PathVariable MatchStatus status,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                matchService.getMatchesByStatus(
                        status,
                        principal.id()));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesByPlayer(
            @PathVariable Long playerId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                matchService.getMatchesByPlayer(
                        playerId,
                        principal.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> updateMatch(
            @PathVariable Long id,
            @RequestBody MatchRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        MatchResponseDTO updatedMatch = matchService.updateMatch(
                id,
                dto,
                principal.id());

        return ResponseEntity.ok(updatedMatch);
    }

    @PutMapping("/{matchId}/sets")
    public ResponseEntity<Void> registerMatchSets(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchScoreRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        matchService.saveMatchSets(
                matchId,
                dto,
                principal.id());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{matchId}/sets")
    public ResponseEntity<List<MatchSetDTO>> getMatchSets(
            @PathVariable Long matchId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                matchService.getSetsByMatchId(
                        matchId,
                        principal.id()));
    }

    @PutMapping("/{id}/wo")
    public ResponseEntity<MatchResponseDTO> registerWalkover(
            @PathVariable Long id,
            @Valid @RequestBody MatchWoRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        MatchResponseDTO response = matchService.registerWalkover(
                id,
                dto,
                principal.id());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> getMatchById(
            @PathVariable Long id,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        MatchResponseDTO match = matchService.getMatchById(
                id,
                principal.id());

        return ResponseEntity.ok(match);
    }
}
