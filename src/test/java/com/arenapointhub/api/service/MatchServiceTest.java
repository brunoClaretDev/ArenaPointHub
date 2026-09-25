package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.MatchScoreRequestDTO;
import com.arenapointhub.api.dto.MatchScoreUpdateDTO;
import com.arenapointhub.api.dto.MatchSetDTO;
import com.arenapointhub.api.dto.MatchWoRequestDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.MatchSet;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.MatchSetRepository;
import com.arenapointhub.api.repository.PlayerRepository;

class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MatchSetRepository matchSetRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GlobalRankingService globalRankingService;

    private MatchService matchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        matchService = new MatchService(
                matchRepository,
                categoryRepository,
                playerRepository,
                matchSetRepository,
                groupRepository,
                globalRankingService
        );
    }

    @Test
    void shouldCreateMatch() {
        Category category = createCategory(1L);
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        MatchRequestDTO dto = createMatchRequest(
                1L,
                1L,
                2L
        );

        when(matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                "Mesa 1",
                "10:00",
                MatchStatus.CANCELED
        )).thenReturn(false);

        when(matchRepository.existsByPlayerBusy(
                1L,
                2L,
                "10:00",
                MatchStatus.CANCELED
        )).thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player1));

        when(playerRepository.findById(2L))
                .thenReturn(Optional.of(player2));

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> {
                    Match match = invocation.getArgument(0);
                    match.setId(10L);
                    return match;
                });

        MatchResponseDTO response = matchService.createMatch(dto);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getCategoryId());
        assertEquals(1L, response.getPlayer1().getId());
        assertEquals(2L, response.getPlayer2().getId());
        assertEquals(MatchStatus.SCHEDULED, response.getStatus());

        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenPlayerIsTheSame() {
        MatchRequestDTO dto = createMatchRequest(
                1L,
                1L,
                1L
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto)
        );

        assertEquals(
                "Um jogador não pode jogar contra ele mesmo.",
                exception.getMessage()
        );

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenCourtIsOccupied() {
        MatchRequestDTO dto = createMatchRequest(
                1L,
                1L,
                2L
        );

        when(matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                "Mesa 1",
                "10:00",
                MatchStatus.CANCELED
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto)
        );

        assertEquals(
                "A mesa/quadra 'Mesa 1' já está ocupada no horário 10:00",
                exception.getMessage()
        );

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenPlayerIsBusy() {
        MatchRequestDTO dto = createMatchRequest(
                1L,
                1L,
                2L
        );

        when(matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                "Mesa 1",
                "10:00",
                MatchStatus.CANCELED
        )).thenReturn(false);

        when(matchRepository.existsByPlayerBusy(
                1L,
                2L,
                "10:00",
                MatchStatus.CANCELED
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto)
        );

        assertEquals(
                "Um dos jogadores já possui partida agendada no horário 10:00",
                exception.getMessage()
        );

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldReturnMatchById() {
        Category category = createCategory(1L);
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.SCHEDULED,
                0,
                0
        );

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(match));

        MatchResponseDTO response = matchService.getMatchById(10L);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getPlayer1().getId());
        assertEquals(2L, response.getPlayer2().getId());

        verify(matchRepository).findById(10L);
    }

    @Test
    void shouldNotReturnMatchWhenIdDoesNotExist() {
        when(matchRepository.findById(10L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.getMatchById(10L)
        );

        assertEquals(
                "Partida não encontrada com ID: 10",
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateScore() {
        Category category = createCategory(1L);
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.IN_PROGRESS,
                1,
                0
        );

        MatchScoreUpdateDTO dto = new MatchScoreUpdateDTO();
        dto.setScorePlayer1(2);
        dto.setScorePlayer2(1);
        dto.setStatus(MatchStatus.FINISHED);

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(match));

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchResponseDTO response =
                matchService.updateScore(10L, dto);

        assertEquals(2, response.getScorePlayer1());
        assertEquals(1, response.getScorePlayer2());
        assertEquals(MatchStatus.FINISHED, response.getStatus());

        verify(matchRepository).save(match);
    }

    @Test
    void shouldSaveValidSetsAndFinishMatch() {
        Category category = createCategory(1L);
        category.setSetsToWinMatch(2);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.IN_PROGRESS,
                0,
                0
        );

        MatchScoreRequestDTO dto = new MatchScoreRequestDTO();

        List<MatchSetDTO> sets = List.of(
                new MatchSetDTO(1, 11, 5),
                new MatchSetDTO(2, 11, 7)
        );

        dto.setSets(sets);

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(match));

        when(matchSetRepository.findByMatchIdAndSetNumber(
                10L,
                1
        )).thenReturn(Optional.empty());

        when(matchSetRepository.findByMatchIdAndSetNumber(
                10L,
                2
        )).thenReturn(Optional.empty());

        when(matchSetRepository.findByMatchId(10L))
                .thenReturn(List.of(
                        createMatchSet(match, 1, 11, 5),
                        createMatchSet(match, 2, 11, 7)
                ));

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        matchService.saveMatchSets(10L, dto);

        assertEquals(2, match.getScorePlayer1());
        assertEquals(0, match.getScorePlayer2());
        assertEquals(MatchStatus.FINISHED, match.getStatus());

        verify(matchSetRepository, times(2))
                .save(any(MatchSet.class));

        verify(matchRepository).save(match);
    }

    @Test
    void shouldNotSaveInvalidSetScore() {
        Category category = createCategory(1L);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.IN_PROGRESS,
                0,
                0
        );

        MatchScoreRequestDTO dto = new MatchScoreRequestDTO();

        dto.setSets(List.of(
                new MatchSetDTO(1, 10, 8)
        ));

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(match));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.saveMatchSets(10L, dto)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Placar inválido para o set 1"
                )
        );

        verify(matchSetRepository, never())
                .save(any(MatchSet.class));
    }

    @Test
    void shouldReturnSetsByMatchId() {
        Category category = createCategory(1L);
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.FINISHED,
                2,
                0
        );

        when(matchSetRepository.findByMatchId(10L))
                .thenReturn(List.of(
                        createMatchSet(match, 1, 11, 5),
                        createMatchSet(match, 2, 11, 7)
                ));

        List<MatchSetDTO> response =
                matchService.getSetsByMatchId(10L);

        assertEquals(2, response.size());
        assertEquals(1, response.get(0).getSetNumber());
        assertEquals(11, response.get(0).getScorePlayer1());
        assertEquals(5, response.get(0).getScorePlayer2());
    }

    @Test
    void shouldRegisterWalkoverForPlayer1() {
        Category category = createCategory(1L);
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.SCHEDULED,
                0,
                0
        );

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(1L);

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(match));

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchResponseDTO response =
                matchService.registerWalkover(10L, dto);

        assertEquals(MatchStatus.WO, response.getStatus());
        assertEquals(2, response.getScorePlayer1());
        assertEquals(0, response.getScorePlayer2());

        verify(matchRepository).save(match);
    }

    @Test
    void shouldNotRegisterWalkoverForPlayerOutsideMatch() {
        Category category = createCategory(1L);
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.SCHEDULED,
                0,
                0
        );

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(3L);

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(match));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.registerWalkover(10L, dto)
        );

        assertEquals(
                "O jogador informado não faz parte desta partida.",
                exception.getMessage()
        );

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldUpdateMatch() {
        Category category = createCategory(1L);
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L,
                category,
                player1,
                player2,
                MatchPhase.GROUP,
                MatchStatus.SCHEDULED,
                0,
                0
        );

        MatchRequestDTO dto = createMatchRequest(
                1L,
                1L,
                2L
        );

        dto.setScorePlayer1(2);
        dto.setScorePlayer2(1);
        dto.setStatus(MatchStatus.FINISHED);

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(match));

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchResponseDTO response =
                matchService.updateMatch(10L, dto);

        assertEquals(2, response.getScorePlayer1());
        assertEquals(1, response.getScorePlayer2());
        assertEquals(MatchStatus.FINISHED, response.getStatus());

        verify(matchRepository).save(match);
    }

    @Test
    void shouldAdvanceWinnerFromQuarterFinalToSemiFinal() {
        Category category = createCategory(1L);

        Player winner = createPlayer(1L, "Winner");
        Player opponent = createPlayer(2L, "Opponent");
        Player other1 = createPlayer(3L, "Other 1");
        Player other2 = createPlayer(4L, "Other 2");

        Match quarter1 = createMatch(
                10L,
                category,
                winner,
                opponent,
                MatchPhase.QUARTER_FINAL,
                MatchStatus.FINISHED,
                3,
                1
        );

        Match quarter2 = createMatch(
                11L,
                category,
                other1,
                other2,
                MatchPhase.QUARTER_FINAL,
                MatchStatus.SCHEDULED,
                0,
                0
        );

        Match semiFinal = createMatch(
                20L,
                category,
                null,
                null,
                MatchPhase.SEMI_FINAL,
                MatchStatus.SCHEDULED,
                0,
                0
        );

        when(matchRepository.findById(10L))
                .thenReturn(Optional.of(quarter1));

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(matchRepository.findByCategoryId(1L))
                .thenReturn(List.of(
                        quarter1,
                        quarter2,
                        semiFinal
                ));

        MatchScoreUpdateDTO dto = new MatchScoreUpdateDTO();
        dto.setScorePlayer1(3);
        dto.setScorePlayer2(1);
        dto.setStatus(MatchStatus.FINISHED);

        matchService.updateScore(10L, dto);

        assertEquals(
                winner,
                semiFinal.getPlayer1()
        );

        verify(matchRepository).save(semiFinal);
    }

    private MatchRequestDTO createMatchRequest(
            Long categoryId,
            Long player1Id,
            Long player2Id) {

        MatchRequestDTO dto = new MatchRequestDTO();
        dto.setCategoryId(categoryId);
        dto.setPlayer1Id(player1Id);
        dto.setPlayer2Id(player2Id);
        dto.setTableOrCourt("Mesa 1");
        dto.setScheduledTime("10:00");

        return dto;
    }

    private Category createCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Sub 15");
        category.setSetsToWinMatch(2);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Tournament Test");

        category.setTournament(tournament);

        return category;
    }

    private Player createPlayer(Long id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setEmail(
                name.toLowerCase().replace(" ", "") + "@email.com"
        );
        player.setBirthDate(LocalDate.of(2012, 5, 10));
        player.setClubAcademy("ArenaPoint");

        return player;
    }

    private Match createMatch(
            Long id,
            Category category,
            Player player1,
            Player player2,
            MatchPhase phase,
            MatchStatus status,
            int scorePlayer1,
            int scorePlayer2) {

        Match match = new Match();
        match.setId(id);
        match.setCategory(category);
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setPhase(phase);
        match.setStatus(status);
        match.setScorePlayer1(scorePlayer1);
        match.setScorePlayer2(scorePlayer2);

        return match;
    }

    private MatchSet createMatchSet(
            Match match,
            int setNumber,
            int scorePlayer1,
            int scorePlayer2) {

        MatchSet matchSet = new MatchSet();
        matchSet.setMatch(match);
        matchSet.setSetNumber(setNumber);
        matchSet.setScorePlayer1(scorePlayer1);
        matchSet.setScorePlayer2(scorePlayer2);

        return matchSet;
    }
}