package com.arenapointhub.api.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.model.enums.TournamentStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            TournamentRepository tournamentRepository,
            CategoryRepository categoryRepository, 
            PlayerRepository playerRepository,
            GroupRepository groupRepository,
            MatchRepository matchRepository) {
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
                Category savedCategory = categoryRepository.save(category);

                // 3. Cadastra 16 jogadores de teste (Ideal para 4 grupos de 4)
                List<Player> savedPlayers = new ArrayList<>();
                for (int i = 1; i <= 16; i++) {
                    Player player = new Player();
                    player.setName("Jogador " + i);
                    player.setEmail("jogador" + i + "@email.com");
                    player.setBirthDate(LocalDate.of(1995, 5, 10)); 
                    player.setClubAcademy("Clube " + (i % 3 == 0 ? "Alpha" : i % 3 == 1 ? "Beta" : "Gama"));
                    savedPlayers.add(playerRepository.save(player));
                }

                // 4. Cria 4 Grupos e vincula à Categoria
                List<Group> savedGroups = new ArrayList<>();
                for (int i = 1; i <= 4; i++) {
                    Group group = new Group();
                    group.setName("Grupo " + (char)('A' + i - 1)); // Gera Grupo A, B, C, D
                    group.setCategory(savedCategory);
                    savedGroups.add(groupRepository.save(group));
                }

                // 5. Cria as partidas finalizadas para testar o chaveamento
                int playerIndex = 0;
                for (Group group : savedGroups) {
                    // Pega 4 jogadores para cada grupo
                    Player p1 = savedPlayers.get(playerIndex++);
                    Player p2 = savedPlayers.get(playerIndex++);
                    Player p3 = savedPlayers.get(playerIndex++);
                    Player p4 = savedPlayers.get(playerIndex++);
                    
                    // Simula os confrontos do grupo todos finalizados
                    createFinishedMatch(matchRepository, savedCategory, group, p1, p2, 2, 0); // P1 vence
                    createFinishedMatch(matchRepository, savedCategory, group, p3, p4, 1, 2); // P4 vence
                    createFinishedMatch(matchRepository, savedCategory, group, p1, p3, 2, 1); // P1 vence (1º do grupo)
                    createFinishedMatch(matchRepository, savedCategory, group, p2, p4, 0, 2); // P4 vence (2º do grupo)
                }

                System.out.println(">>> Dados de teste carregados! Torneio, Categoria, Grupos, Jogadores e Partidas criados.");
            }
        };
    }

    // Método auxiliar para não poluir o código principal
    private void createFinishedMatch(MatchRepository matchRepository, Category category, Group group, Player p1, Player p2, int score1, int score2) {
        Match match = new Match();
        match.setCategory(category);
        match.setGroup(group);
        match.setPlayer1(p1);
        match.setPlayer2(p2);
        match.setPhase(MatchPhase.GROUP);
        match.setStatus(MatchStatus.FINISHED);
        match.setScorePlayer1(score1);
        match.setScorePlayer2(score2);
        matchRepository.save(match);
    }
}