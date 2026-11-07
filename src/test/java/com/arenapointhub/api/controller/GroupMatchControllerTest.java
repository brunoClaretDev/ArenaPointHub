
package com.arenapointhub.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.GroupMatchService;

@ExtendWith(MockitoExtension.class)
class GroupMatchControllerTest {

    private static final Long ADMINISTRATOR_ID = 100L;

    @Mock
    private GroupMatchService groupMatchService;

    private GroupMatchController groupMatchController;
    private AdministratorPrincipal principal;

    @BeforeEach
    void setUp() {
        groupMatchController = new GroupMatchController(groupMatchService);

        principal = new AdministratorPrincipal(
                ADMINISTRATOR_ID,
                "admin",
                "password",
                true);
    }

    @Test
    void shouldGenerateMatchesWithTableAndScheduledTime() {
        Long groupId = 1L;
        String tableOrCourt = "Mesa 1";
        String scheduledTime = "2026-10-01T14:00";

        List<MatchResponseDTO> expected = List.of(new MatchResponseDTO());

        when(groupMatchService.generateRoundRobinMatchesForGroup(
                groupId,
                tableOrCourt,
                scheduledTime,
                ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                groupMatchController.generateMatches(
                        groupId,
                        tableOrCourt,
                        scheduledTime,
                        principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupMatchService).generateRoundRobinMatchesForGroup(
                groupId,
                tableOrCourt,
                scheduledTime,
                ADMINISTRATOR_ID);
    }

    @Test
    void shouldGenerateMatchesWithoutOptionalParameters() {
        Long groupId = 1L;
        List<MatchResponseDTO> expected = List.of();

        when(groupMatchService.generateRoundRobinMatchesForGroup(
                groupId,
                null,
                null,
                ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<MatchResponseDTO>> response =
                groupMatchController.generateMatches(
                        groupId,
                        null,
                        null,
                        principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupMatchService).generateRoundRobinMatchesForGroup(
                groupId,
                null,
                null,
                ADMINISTRATOR_ID);
    }
}
