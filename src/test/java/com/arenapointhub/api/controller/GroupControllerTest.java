
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
                true);
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
        Long categoryId = 1L;
        List<GroupResponseDTO> expected = List.of(new GroupResponseDTO());

        when(groupService.getGroupsByCategory(categoryId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<GroupResponseDTO>> response =
                groupController.getGroupsByCategory(categoryId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).getGroupsByCategory(categoryId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetGroupById() {
        Long groupId = 1L;
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.getGroupById(groupId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.getGroupById(groupId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).getGroupById(groupId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldUpdateGroup() {
        Long groupId = 1L;
        GroupRequestDTO request = new GroupRequestDTO();
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.updateGroup(groupId, request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.updateGroup(groupId, request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).updateGroup(groupId, request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGenerateGroups() {
        GroupGenerateRequestDTO request = new GroupGenerateRequestDTO();
        List<GroupResponseDTO> expected = List.of(new GroupResponseDTO());

        when(groupService.generateAndDistributeGroups(request, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<GroupResponseDTO>> response =
                groupController.generateGroups(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).generateAndDistributeGroups(request, ADMINISTRATOR_ID);
    }

    @Test
    void shouldGetGroupStandings() {
        Long groupId = 1L;
        List<GroupStandingDTO> expected = List.of(new GroupStandingDTO());

        when(groupService.calculateGroupStandings(groupId, ADMINISTRATOR_ID))
                .thenReturn(expected);

        ResponseEntity<List<GroupStandingDTO>> response =
                groupController.getGroupStandings(groupId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).calculateGroupStandings(groupId, ADMINISTRATOR_ID);
    }

    @Test
    void shouldRemovePlayerFromGroup() {
        Long groupId = 1L;
        Long playerId = 2L;

        ResponseEntity<Void> response =
                groupController.removePlayerFromGroup(
                        groupId,
                        playerId,
                        principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(groupService).removePlayerFromGroup(
                groupId,
                playerId,
                ADMINISTRATOR_ID);
    }
}
