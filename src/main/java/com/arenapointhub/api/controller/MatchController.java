package com.arenapointhub.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<MatchResponseDTO> createMatch(@Valid @RequestBody MatchRequestDTO dto) {
		MatchResponseDTO created = matchService.createMatch(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public ResponseEntity<List<MatchResponseDTO>> getAllMatches() {
		return ResponseEntity.ok(matchService.getAllMatches());
	}

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<MatchResponseDTO>> getMatchesByCategory(@PathVariable Long categoryId) {
		return ResponseEntity.ok(matchService.getMatchesByCategory(categoryId));
	}

	@PatchMapping("/{id}/score")
	public ResponseEntity<MatchResponseDTO> updateScore(@PathVariable Long id,
			@Valid @RequestBody MatchScoreUpdateDTO dto) {
		MatchResponseDTO updated = matchService.updateScore(id, dto);
		return ResponseEntity.ok(updated);
	}
	
	@GetMapping("/status/{status}")
	public ResponseEntity<List<MatchResponseDTO>> getMatchesByStatus(@PathVariable MatchStatus status) {
	    return ResponseEntity.ok(matchService.getMatchesByStatus(status));
	}

	@GetMapping("/player/{playerId}")
	public ResponseEntity<List<MatchResponseDTO>> getMatchesByPlayer(@PathVariable Long playerId) {
	    return ResponseEntity.ok(matchService.getMatchesByPlayer(playerId));
	}
	
	@PutMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> updateMatch(@PathVariable Long id, @RequestBody MatchRequestDTO dto) {
        MatchResponseDTO updatedMatch = matchService.updateMatch(id, dto);
        return ResponseEntity.ok(updatedMatch);
    }

	// ==========================================
	// NOVOS ENDPOINTS PARA GERENCIAMENTO DE SETS
	// ==========================================

	@PutMapping("/{matchId}/sets")
	public ResponseEntity<Void> registerMatchSets(
			@PathVariable Long matchId, 
			@Valid @RequestBody MatchScoreRequestDTO dto) {
		matchService.saveMatchSets(matchId, dto);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{matchId}/sets")
	public ResponseEntity<List<MatchSetDTO>> getMatchSets(@PathVariable Long matchId) {
		return ResponseEntity.ok(matchService.getSetsByMatchId(matchId));
	}
	
	@PutMapping("/{id}/wo")
	public ResponseEntity<MatchResponseDTO> registerWalkover(
	        @PathVariable Long id, 
	        @RequestBody @Valid MatchWoRequestDTO dto) {
	    MatchResponseDTO response = matchService.registerWalkover(id, dto);
	    return ResponseEntity.ok(response);
	}
}