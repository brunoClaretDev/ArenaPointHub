package com.arenapointhub.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.GroupGenerateRequestDTO;
import com.arenapointhub.api.dto.GroupRequestDTO;
import com.arenapointhub.api.dto.GroupResponseDTO;
import com.arenapointhub.api.dto.GroupStandingDTO;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.GroupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponseDTO> createGroup(
            @Valid @RequestBody GroupRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        GroupResponseDTO createdGroup =
                groupService.createGroup(dto, principal.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<GroupResponseDTO>> getGroupsByCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                groupService.getGroupsByCategory(categoryId, principal.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> getGroupById(
            @PathVariable Long id,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        return ResponseEntity.ok(
                groupService.getGroupById(id, principal.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody GroupRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        GroupResponseDTO updatedGroup =
                groupService.updateGroup(id, dto, principal.id());

        return ResponseEntity.ok(updatedGroup);
    }

    @PostMapping("/generate")
    public ResponseEntity<List<GroupResponseDTO>> generateGroups(
            @RequestBody GroupGenerateRequestDTO dto,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        List<GroupResponseDTO> generatedGroups =
                groupService.generateAndDistributeGroups(dto, principal.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(generatedGroups);
    }

    @GetMapping("/{groupId}/standings")
    public ResponseEntity<List<GroupStandingDTO>> getGroupStandings(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        List<GroupStandingDTO> standings =
                groupService.calculateGroupStandings(groupId, principal.id());

        return ResponseEntity.ok(standings);
    }

    @DeleteMapping("/{groupId}/players/{playerId}")
    public ResponseEntity<Void> removePlayerFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long playerId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        groupService.removePlayerFromGroup(
                groupId, playerId, principal.id());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/category/{categoryId}/generate-matches")
    public ResponseEntity<Void> generateMatchesForCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        groupService.generateMatchesForCategory(
                categoryId, principal.id());

        return ResponseEntity.ok().build();
    }
    
    
}