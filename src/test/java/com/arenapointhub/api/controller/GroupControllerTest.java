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
import com.arenapointhub.api.service.GroupService;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private GroupService groupService;

    private GroupController groupController;

    @BeforeEach
    void setUp() {
        groupController = new GroupController(groupService);
    }

    @Test
    void shouldCreateGroup() {
        GroupRequestDTO request = new GroupRequestDTO();
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.createGroup(request)).thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.createGroup(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).createGroup(request);
    }

    @Test
    void shouldGetGroupsByCategory() {
        Long categoryId = 1L;
        List<GroupResponseDTO> expected = List.of(new GroupResponseDTO());

        when(groupService.getGroupsByCategory(categoryId)).thenReturn(expected);

        ResponseEntity<List<GroupResponseDTO>> response =
                groupController.getGroupsByCategory(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).getGroupsByCategory(categoryId);
    }

    @Test
    void shouldGetGroupById() {
        Long groupId = 1L;
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.getGroupById(groupId)).thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.getGroupById(groupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).getGroupById(groupId);
    }

    @Test
    void shouldUpdateGroup() {
        Long groupId = 1L;
        GroupRequestDTO request = new GroupRequestDTO();
        GroupResponseDTO expected = new GroupResponseDTO();

        when(groupService.updateGroup(groupId, request)).thenReturn(expected);

        ResponseEntity<GroupResponseDTO> response =
                groupController.updateGroup(groupId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).updateGroup(groupId, request);
    }

    @Test
    void shouldGenerateGroups() {
        GroupGenerateRequestDTO request = new GroupGenerateRequestDTO();
        List<GroupResponseDTO> expected = List.of(new GroupResponseDTO());

        when(groupService.generateAndDistributeGroups(request))
                .thenReturn(expected);

        ResponseEntity<List<GroupResponseDTO>> response =
                groupController.generateGroups(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).generateAndDistributeGroups(request);
    }

    @Test
    void shouldGetGroupStandings() {
        Long groupId = 1L;
        List<GroupStandingDTO> expected = List.of(new GroupStandingDTO());

        when(groupService.calculateGroupStandings(groupId)).thenReturn(expected);

        ResponseEntity<List<GroupStandingDTO>> response =
                groupController.getGroupStandings(groupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(groupService).calculateGroupStandings(groupId);
    }

    @Test
    void shouldRemovePlayerFromGroup() {
        Long groupId = 1L;
        Long playerId = 2L;

        ResponseEntity<Void> response =
                groupController.removePlayerFromGroup(groupId, playerId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(groupService).removePlayerFromGroup(groupId, playerId);
    }
}