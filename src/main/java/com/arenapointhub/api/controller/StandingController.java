package com.arenapointhub.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.StandingDTO;
import com.arenapointhub.api.service.StandingService;

@RestController
@RequestMapping("/api/groups")
public class StandingController {

    @Autowired
    private StandingService standingService;

    @GetMapping("/{groupId}/standings")
    public ResponseEntity<List<StandingDTO>> getGroupStandings(@PathVariable Long groupId) {
        List<StandingDTO> standings = standingService.calculateGroupStandings(groupId);
        return ResponseEntity.ok(standings);
    }
}