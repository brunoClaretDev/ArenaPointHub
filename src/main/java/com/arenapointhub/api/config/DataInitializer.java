package com.arenapointhub.api.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@Configuration
public class DataInitializer {

    @Value("${arenapoint.initializer.enabled:false}")
    private boolean initializerEnabled;

    @Bean
    public CommandLineRunner initData(
            TournamentRepository tournamentRepository,
            CategoryRepository categoryRepository,
            PlayerRepository playerRepository) {

        return args -> {

            if (!initializerEnabled) {
                return;
            }

            if (tournamentRepository.count() > 0) {
                System.out.println(">>> Dados de teste já existem. Inicialização ignorada.");
                return;
            }

            Tournament tournament = new Tournament();
            tournament.setName("Torneio Aberto Arena Point");
            tournament.setDescription("Torneio de testes principal");
            tournament.setLocation("São Paulo - SP");
            tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);

            Tournament savedTournament = tournamentRepository.save(tournament);

            Category category = new Category();
            category.setName("Categoria A - Livre");
            category.setTournament(savedTournament);

            categoryRepository.save(category);

            for (int i = 1; i <= 16; i++) {
                Player player = new Player();
                player.setName("Jogador " + i);
                player.setEmail("jogador" + i + "@email.com");
                player.setBirthDate(LocalDate.of(1995, 5, 10));
                player.setClubAcademy(
                        "Clube " + (i % 3 == 0 ? "Alpha" : i % 3 == 1 ? "Beta" : "Gama"));

                playerRepository.save(player);
            }

            System.out.println(
                    ">>> Dados de teste carregados: 1 torneio, 1 categoria e 16 jogadores.");
        };
    }
}