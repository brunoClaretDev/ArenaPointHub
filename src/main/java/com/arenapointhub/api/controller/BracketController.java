
package com.arenapointhub.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.BracketService;

@RestController
@RequestMapping("/api/brackets")
public class BracketController {

    private final BracketService bracketService;

    public BracketController(BracketService bracketService) {
        this.bracketService = bracketService;
    }

    @PostMapping("/generate/{categoryId}")
    public ResponseEntity<BracketResponseDTO> generateKnockoutBracket(
            @PathVariable("categoryId") Long categoryId,
            @AuthenticationPrincipal AdministratorPrincipal principal) {

        BracketResponseDTO response = bracketService.generateKnockoutBracket(
                categoryId,
                principal.id());

        return ResponseEntity.ok(response);
    }
}
