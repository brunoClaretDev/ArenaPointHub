package com.arenapointhub.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.service.BracketService;

@ExtendWith(MockitoExtension.class)
class BracketControllerTest {

    @Mock
    private BracketService bracketService;

    private BracketController bracketController;

    @BeforeEach
    void setUp() {
        bracketController = new BracketController(bracketService);
    }

    @Test
    void shouldGenerateKnockoutBracket() {
        Long categoryId = 1L;
        int targetBracketSize = 8;

        BracketResponseDTO expectedResponse = new BracketResponseDTO();
        expectedResponse.setCategoryId(categoryId);
        expectedResponse.setCategoryName("Categoria Sub-13");
        expectedResponse.setTargetBracketSize(targetBracketSize);
        expectedResponse.setMatches(Collections.emptyList());

        when(bracketService.generateKnockoutBracket(categoryId, targetBracketSize))
                .thenReturn(expectedResponse);

        ResponseEntity<BracketResponseDTO> response =
                bracketController.generateKnockoutBracket(categoryId, targetBracketSize);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(bracketService).generateKnockoutBracket(categoryId, targetBracketSize);
    }
}