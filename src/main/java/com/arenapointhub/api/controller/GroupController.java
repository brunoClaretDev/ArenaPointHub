package com.arenapointhub.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.GroupGenerateRequestDTO;
import com.arenapointhub.api.dto.GroupRequestDTO;
import com.arenapointhub.api.dto.GroupResponseDTO;
import com.arenapointhub.api.dto.GroupStandingDTO;
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
    public ResponseEntity<GroupResponseDTO> createGroup(@Valid @RequestBody GroupRequestDTO dto) {
        GroupResponseDTO createdGroup = groupService.createGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<GroupResponseDTO>> getGroupsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(groupService.getGroupsByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }
    
    @PostMapping("/generate")
    public ResponseEntity<List<GroupResponseDTO>> generateGroups(@RequestBody GroupGenerateRequestDTO dto) {
        List<GroupResponseDTO> generatedGroups = groupService.generateAndDistributeGroups(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(generatedGroups);
    }
    
    @GetMapping("/{groupId}/standings")
    public ResponseEntity<List<GroupStandingDTO>> getGroupStandings(@PathVariable Long groupId) {
        List<GroupStandingDTO> standings = groupService.calculateGroupStandings(groupId);
        return ResponseEntity.ok(standings);
    }
    
    @DeleteMapping("/{groupId}/players/{playerId}")
    public ResponseEntity<Void> removePlayerFromGroup(
            @PathVariable Long groupId, 
            @PathVariable Long playerId) {
        groupService.removePlayerFromGroup(groupId, playerId);
        return ResponseEntity.noContent().build();
    }
}