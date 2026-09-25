package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Tournament;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CategoryRepositoryTest {

    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;

    CategoryRepositoryTest(
            CategoryRepository categoryRepository,
            TournamentRepository tournamentRepository) {

        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Test
    void shouldSaveAndFindCategory() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament = tournamentRepository.save(tournament);

        Category category = new Category();
        category.setName("Categoria Teste");
        category.setTournament(savedTournament);

        Category savedCategory = categoryRepository.save(category);

        Optional<Category> foundCategory =
                categoryRepository.findById(savedCategory.getId());

        assertTrue(foundCategory.isPresent());
        assertEquals("Categoria Teste", foundCategory.get().getName());
    }

    @Test
    void shouldFindCategoriesByTournamentId() {
        Tournament tournament = new Tournament();
        tournament.setName("Torneio Teste");

        Tournament savedTournament = tournamentRepository.save(tournament);

        Category category1 = new Category();
        category1.setName("Categoria A");
        category1.setTournament(savedTournament);

        Category category2 = new Category();
        category2.setName("Categoria B");
        category2.setTournament(savedTournament);

        categoryRepository.save(category1);
        categoryRepository.save(category2);

        List<Category> categories =
                categoryRepository.findByTournamentId(savedTournament.getId());

        assertEquals(2, categories.size());
        assertEquals("Categoria A", categories.get(0).getName());
        assertEquals("Categoria B", categories.get(1).getName());
    }
}