package com.arenapointhub.api.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.TournamentScoringConfig;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;
import com.arenapointhub.api.repository.TournamentScoringConfigRepository;

@Configuration
public class DataInitializer {

    @Value("${arenapoint.initializer.enabled:false}")
    private boolean initializerEnabled;

    @Bean
    public CommandLineRunner initData(
            TournamentRepository tournamentRepository,
            CategoryRepository categoryRepository,
            PlayerRepository playerRepository,
            TournamentScoringConfigRepository scoringConfigRepository) {

        return args -> {
            if (!initializerEnabled) {
                return;
            }

            if (tournamentRepository.count() > 0) {
                System.out.println(
                        ">>> Dados de teste já existem. Inicialização ignorada.");
                return;
            }

            Tournament tournament = createTournament();
            Tournament savedTournament = tournamentRepository.save(tournament);

            createScoringConfig(savedTournament, scoringConfigRepository);

            Category category = createCategory(savedTournament);
            Category savedCategory = categoryRepository.save(category);

            addPlayersToCategory(savedCategory, playerRepository);
            categoryRepository.save(savedCategory);

            System.out.println(
                    ">>> Dados de teste carregados: 1 torneio, 1 categoria, "
                    + "16 jogadores e 1 configuração de pontuação.");
        };
    }

    private Tournament createTournament() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Aberto Arena Point");
        tournament.setDescription("Torneio de testes principal");
        tournament.setLocation("São Paulo - SP");
        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);

        return tournament;
    }

    private void createScoringConfig(
            Tournament tournament,
            TournamentScoringConfigRepository scoringConfigRepository) {

        TournamentScoringConfig scoringConfig = new TournamentScoringConfig();
        scoringConfig.setTournament(tournament);

        scoringConfigRepository.save(scoringConfig);
    }

    private Category createCategory(Tournament tournament) {
        Category category = new Category();
        category.setName("Categoria A - Livre");
        category.setTournament(tournament);

        return category;
    }

    private void addPlayersToCategory(
            Category category,
            PlayerRepository playerRepository) {

        for (int i = 1; i <= 16; i++) {
            Player player = createPlayer(i);
            Player savedPlayer = playerRepository.save(player);

            category.getPlayers().add(savedPlayer);
        }
    }

    private Player createPlayer(int number) {
        Player player = new Player();
        player.setName("Jogador " + number);
        player.setEmail("jogador" + number + "@email.com");
        player.setBirthDate(LocalDate.of(1995, 5, 10));
        player.setClubAcademy(getClubName(number));

        return player;
    }

    private String getClubName(int number) {
        if (number % 3 == 0) {
            return "Clube Alpha";
        }

        if (number % 3 == 1) {
            return "Clube Beta";
        }

        return "Clube Gama";
    }
}