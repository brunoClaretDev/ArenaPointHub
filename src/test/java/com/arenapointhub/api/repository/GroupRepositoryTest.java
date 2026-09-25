package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GroupRepositoryTest {

    private final GroupRepository groupRepository;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;

    GroupRepositoryTest(
            GroupRepository groupRepository,
            CategoryRepository categoryRepository,
            TournamentRepository tournamentRepository,
            PlayerRepository playerRepository) {

        this.groupRepository = groupRepository;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
    }

    @Test
    void shouldFindGroupsByCategoryId() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament = tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory = categoryRepository.save(category);

        Group groupA = new Group();
        groupA.setName("Grupo A");
        groupA.setCategory(savedCategory);

        Group groupB = new Group();
        groupB.setName("Grupo B");
        groupB.setCategory(savedCategory);

        groupRepository.save(groupA);
        groupRepository.save(groupB);

        List<Group> groups =
                groupRepository.findByCategoryId(savedCategory.getId());

        assertEquals(2, groups.size());
        assertEquals("Grupo A", groups.get(0).getName());
        assertEquals("Grupo B", groups.get(1).getName());
    }

    @Test
    void shouldCheckIfPlayerExistsInCategoryGroup() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament = tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory = categoryRepository.save(category);

        Player player = new Player();
        player.setName("Jogador Teste");
        player.setEmail("jogador.teste@email.com");
        player.setBirthDate(LocalDate.of(2012, 5, 10));

        Player savedPlayer = playerRepository.save(player);

        Group group = new Group();
        group.setName("Grupo A");
        group.setCategory(savedCategory);
        group.setPlayers(List.of(savedPlayer));

        groupRepository.save(group);

        boolean playerExists =
                groupRepository.existsByCategoryIdAndPlayersId(
                        savedCategory.getId(),
                        savedPlayer.getId());

        assertEquals(true, playerExists);
    }
}