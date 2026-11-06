
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
import com.arenapointhub.api.security.AdministratorPrincipal;
import com.arenapointhub.api.service.BracketService;

@ExtendWith(MockitoExtension.class)
class BracketControllerTest {

    private static final Long CATEGORY_ID = 1L;
    private static final Long ADMINISTRATOR_ID = 10L;

    @Mock
    private BracketService bracketService;

    private BracketController bracketController;

    private AdministratorPrincipal principal;

    @BeforeEach
    void setUp() {
        bracketController = new BracketController(bracketService);

        principal = new AdministratorPrincipal(
                ADMINISTRATOR_ID,
                "admin",
                "password",
                true);
    }

    @Test
    void shouldGenerateKnockoutBracket() {

        BracketResponseDTO expectedResponse = new BracketResponseDTO();

        expectedResponse.setCategoryId(CATEGORY_ID);
        expectedResponse.setCategoryName("Categoria Sub-13");
        expectedResponse.setTargetBracketSize(8);
        expectedResponse.setMatches(Collections.emptyList());

        when(bracketService.generateKnockoutBracket(
                CATEGORY_ID,
                ADMINISTRATOR_ID))
                .thenReturn(expectedResponse);

        ResponseEntity<BracketResponseDTO> response =
                bracketController.generateKnockoutBracket(
                        CATEGORY_ID,
                        principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(bracketService).generateKnockoutBracket(
                CATEGORY_ID,
                ADMINISTRATOR_ID);
    }
}
