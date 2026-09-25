package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Tournament;

@DataJpaTest
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

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
}