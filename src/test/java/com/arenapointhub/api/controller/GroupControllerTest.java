
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

import com.arenapointhub.api.dto.GroupGenerateRequestDTO;
import com.arenapointhub.api.dto.GroupRequestDTO;
import com.arenapointhub.api.dto.GroupResponseDTO;
import com.arenapointhub.api.dto.GroupStandingDTO;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.GroupService;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    private static final Long ADMINISTRATOR_ID = 10L;

    private static final Long CATEGORY_ID = 1L;
    private static final Long GROUP_ID = 1L;
    private static final Long PLAYER_ID = 2L;

    @Mock
    private GroupService groupService;

    private GroupController groupController;

    private AdministratorPrincipal principal;

    @BeforeEach
    void setUp() {
        groupController = new GroupController(groupService);

        principal = new AdministratorPrincipal(
                ADMINISTRATOR_ID,
                "admin",
                "password",
                true
        );
    }

    @Test
    void shouldCreateGroup() {
        GroupRequestDTO request = new GroupRequestDTO();
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.createGroup(request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.createGroup(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).createGroup(request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetGroupsByCategory() {
        List<GroupResponseDTO> expected =
                List.of(new GroupResponseDTO());

        when(groupService.getGroupsByCategory(
                CATEGORY_ID, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<GroupResponseDTO>> response =
                groupController.getGroupsByCategory(CATEGORY_ID, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).getGroupsByCategory(
                CATEGORY_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetGroupById() {
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.getGroupById(GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.getGroupById(GROUP_ID, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).getGroupById(GROUP_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldUpdateGroup() {
        GroupRequestDTO request = new GroupRequestDTO();
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.updateGroup(
                GROUP_ID, request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.updateGroup(GROUP_ID, request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).updateGroup(
                GROUP_ID, request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGenerateGroups() {
        GroupGenerateRequestDTO request = new GroupGenerateRequestDTO();

        List<GroupResponseDTO> expected =
                List.of(new GroupResponseDTO());

        when(groupService.generateAndDistributeGroups(
                request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<GroupResponseDTO>> response =
                groupController.generateGroups(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).generateAndDistributeGroups(
                request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetGroupStandings() {
        List<GroupStandingDTO> expected =
                List.of(new GroupStandingDTO());

        when(groupService.calculateGroupStandings(
                GROUP_ID, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<GroupStandingDTO>> response =
                groupController.getGroupStandings(GROUP_ID, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).calculateGroupStandings(
                GROUP_ID, ADMINISTRATOR_ID);
    }

    @Test
    void shouldRemovePlayerFromGroup() {
        ResponseEntity<Void> response =
                groupController.removePlayerFromGroup(
                        GROUP_ID,
                        PLAYER_ID,
                        principal
                );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(groupService).removePlayerFromGroup(
                GROUP_ID,
                PLAYER_ID,
                ADMINISTRATOR_ID
        );
    }

    @Test
    void shouldGenerateMatchesForCategory() {
        ResponseEntity<Void> response =
                groupController.generateMatchesForCategory(
                        CATEGORY_ID,
                        principal
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(groupService).generateMatchesForCategory(
                CATEGORY_ID,
                ADMINISTRATOR_ID
        );
    }
}
