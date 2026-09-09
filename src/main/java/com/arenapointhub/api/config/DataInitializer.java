package com.arenapointhub.api.config;

import java.time.LocalDate;

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

    @Bean
    public CommandLineRunner initData(
            TournamentRepository tournamentRepository,
            CategoryRepository categoryRepository, 
            PlayerRepository playerRepository) {
        return args -> {
            if (tournamentRepository.count() == 0) {
                // 1. Cria um Torneio de Exemplo
                Tournament tournament = new Tournament();
                tournament.setName("Torneio Aberto Arena Point");
                tournament.setDescription("Torneio de testes principal");
                tournament.setLocation("São Paulo - SP");
                tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
                Tournament savedTournament = tournamentRepository.save(tournament);

                // 2. Cria uma Categoria associada ao Torneio
                Category category = new Category();
                category.setName("Categoria A - Livre");
                category.setTournament(savedTournament);
                categoryRepository.save(category);

                // 3. Cadastra 22 jogadores de teste com data de nascimento preenchida
                for (int i = 1; i <= 22; i++) {
                    Player player = new Player();
                    player.setName("Jogador " + i);
                    player.setEmail("jogador" + i + "@email.com");
                    player.setBirthDate(LocalDate.of(1995, 5, 10)); // Campo obrigatório preenchido
                    player.setClubAcademy("Clube " + (i % 3 == 0 ? "Alpha" : i % 3 == 1 ? "Beta" : "Gama"));
                    playerRepository.save(player);
                }

                System.out.println(">>> Dados de teste carregados com sucesso! Torneio, Categoria e 22 jogadores criados.");
            }
        };
    }
}