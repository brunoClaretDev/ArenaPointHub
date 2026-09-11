package com.arenapointhub.api.controller;

import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;
import com.arenapointhub.api.service.GlobalRankingService;
import com.arenapointhub.api.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final GlobalRankingService globalRankingService;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;

    public TournamentController(TournamentService tournamentService,
                                GlobalRankingService globalRankingService,
                                TournamentRepository tournamentRepository,
                                PlayerRepository playerRepository) {
        this.tournamentService = tournamentService;
        this.globalRankingService = globalRankingService;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
    }

    @PostMapping
    public ResponseEntity<TournamentResponseDTO> createTournament(@Valid @RequestBody TournamentRequestDTO dto) {
        TournamentResponseDTO created = tournamentService.createTournament(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TournamentResponseDTO>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTournamentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> updateTournament(@PathVariable Long id, @Valid @RequestBody TournamentRequestDTO dto) {
        return ResponseEntity.ok(tournamentService.updateTournament(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate-playoffs")
    public ResponseEntity<String> generatePlayoffs(@PathVariable Long id) {
        return ResponseEntity.ok("Playoffs gerados com sucesso para o torneio ID: " + id);
    }

    @PostMapping("/{id}/test-distribute-points")
    public ResponseEntity<String> testDistributePoints(@PathVariable Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneio não encontrado com ID: " + id));

        List<Player> players = playerRepository.findAll();
        if (players.size() < 2) {
            return ResponseEntity.badRequest().body("O banco precisa ter pelo menos 2 jogadores cadastrados.");
        }

        Map<Player, Integer> positions = new HashMap<>();
        positions.put(players.get(0), 1); // 1º Lugar (Campeão)
        positions.put(players.get(1), 2); // 2º Lugar (Vice)

        globalRankingService.distributeTournamentPoints(tournament, positions);

        return ResponseEntity.ok("Pontos distribuídos com sucesso para o ranking global!");
    }
}