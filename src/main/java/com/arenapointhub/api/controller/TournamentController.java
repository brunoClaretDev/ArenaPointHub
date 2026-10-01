package com.arenapointhub.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.TournamentRequestDTO;
import com.arenapointhub.api.dto.TournamentResponseDTO;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;
import com.arenapointhub.api.service.GlobalRankingService;
import com.arenapointhub.api.service.TournamentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final GlobalRankingService globalRankingService;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final CategoryRepository categoryRepository;

    public TournamentController(
            TournamentService tournamentService,
            GlobalRankingService globalRankingService,
            TournamentRepository tournamentRepository,
            PlayerRepository playerRepository,
            CategoryRepository categoryRepository) {

        this.tournamentService = tournamentService;
        this.globalRankingService = globalRankingService;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ResponseEntity<TournamentResponseDTO> createTournament(
            @Valid @RequestBody TournamentRequestDTO dto) {

        TournamentResponseDTO created =
                tournamentService.createTournament(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TournamentResponseDTO>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> getTournamentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(tournamentService.getTournamentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> updateTournament(
            @PathVariable Long id,
            @Valid @RequestBody TournamentRequestDTO dto) {

        return ResponseEntity.ok(tournamentService.updateTournament(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate-playoffs")
    public ResponseEntity<String> generatePlayoffs(@PathVariable Long id) {
        return ResponseEntity.ok(
                "Playoffs gerados com sucesso para o torneio ID: " + id
        );
    }

    @PostMapping("/{id}/test-distribute-points")
    public ResponseEntity<String> testDistributePoints(
            @PathVariable Long id) {

        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Torneio não encontrado com ID: " + id
                ));

        List<Category> categories =
                categoryRepository.findByTournamentId(id);

        if (categories.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("O torneio não possui categorias cadastradas.");
        }

        Category category = categories.get(0);

        List<Player> players = playerRepository.findAll();

        if (players.size() < 2) {
            return ResponseEntity.badRequest().body(
                    "O banco precisa ter pelo menos 2 jogadores cadastrados."
            );
        }

        Map<Player, Integer> positions = new HashMap<>();
        positions.put(players.get(0), 1);
        positions.put(players.get(1), 2);

        globalRankingService.distributeTournamentPoints(
                tournament,
                category,
                positions
        );

        return ResponseEntity.ok(
                "Pontos distribuídos com sucesso para o ranking global!"
        );
    }
}