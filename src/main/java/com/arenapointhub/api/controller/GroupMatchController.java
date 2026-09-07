package com.arenapointhub.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.service.GroupMatchService;

@RestController
@RequestMapping("/api/groups")
public class GroupMatchController {

    @Autowired
    private GroupMatchService groupMatchService;

    @PostMapping("/{groupId}/generate-matches")
    public ResponseEntity<List<MatchResponseDTO>> generateMatches(
            @PathVariable Long groupId,
            @RequestParam(required = false) String tableOrCourt,
            @RequestParam(required = false) String scheduledTime) {
        
        List<MatchResponseDTO> matches = groupMatchService.generateRoundRobinMatchesForGroup(groupId, tableOrCourt, scheduledTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(matches);
    }
}