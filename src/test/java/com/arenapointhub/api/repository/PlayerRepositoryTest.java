package com.arenapointhub.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import com.arenapointhub.api.model.Player;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PlayerRepositoryTest {

    private final PlayerRepository playerRepository;

    PlayerRepositoryTest(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Test
    void shouldCheckIfPlayerExistsByEmail() {
        Player player = new Player();
        player.setName("Jogador Teste");
        player.setEmail("jogador.teste@email.com");
        player.setBirthDate(LocalDate.of(2012, 5, 10));

        playerRepository.save(player);

        boolean exists =
                playerRepository.existsByEmail(
                        "jogador.teste@email.com");

        assertEquals(true, exists);
    }
    
    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists =
                playerRepository.existsByEmail(
                        "email.inexistente@email.com");

        assertEquals(false, exists);
    }
    
}