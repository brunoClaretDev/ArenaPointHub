
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arenapointhub.api.dto.MatchRequestDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.MatchScoreRequestDTO;
import com.arenapointhub.api.dto.MatchScoreUpdateDTO;
import com.arenapointhub.api.dto.MatchSetDTO;
import com.arenapointhub.api.dto.MatchWoRequestDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Administrator;
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

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    private static final Long ADMINISTRATOR_ID = 100L;
    private static final Long CATEGORY_ID = 1L;

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

    @InjectMocks
    private MatchService matchService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = createCategory(CATEGORY_ID);
    }

    @Test
    void shouldCreateMatch() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);

        mockAvailableSchedule();
        mockCategoryExists();
        mockPlayers(player1, player2);
        mockMatchSave();

        MatchResponseDTO response =
                matchService.createMatch(dto, ADMINISTRATOR_ID);

        assertEquals(10L, response.getId());
        assertEquals(CATEGORY_ID, response.getCategoryId());
        assertEquals(1L, response.getPlayer1().getId());
        assertEquals(2L, response.getPlayer2().getId());
        assertEquals(MatchStatus.SCHEDULED, response.getStatus());

        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenPlayerIsTheSame() {
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto, ADMINISTRATOR_ID));

        assertEquals(
                "Um jogador não pode jogar contra ele mesmo.",
                exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenCourtIsOccupied() {
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);

        mockCategoryExists();

        when(matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                "Mesa 1",
                "10:00",
                MatchStatus.CANCELED))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto, ADMINISTRATOR_ID));

        assertEquals(
                "A mesa/quadra 'Mesa 1' já está ocupada no horário 10:00",
                exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenPlayerIsBusy() {
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);

        mockCategoryExists();

        when(matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                "Mesa 1",
                "10:00",
                MatchStatus.CANCELED))
                .thenReturn(false);

        when(matchRepository.existsByPlayerBusy(
                1L,
                2L,
                "10:00",
                MatchStatus.CANCELED))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto, ADMINISTRATOR_ID));

        assertEquals(
                "Um dos jogadores já possui partida agendada no horário 10:00",
                exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenCategoryDoesNotExist() {
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);

        when(categoryRepository.findByIdAndTournament_Administrator_Id(
                CATEGORY_ID,
                ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto, ADMINISTRATOR_ID));

        assertEquals("Categoria não encontrada: 1", exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenPlayer1DoesNotExist() {
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);

        mockAvailableSchedule();
        mockCategoryExists();

        when(playerRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto, ADMINISTRATOR_ID));

        assertEquals("Jogador 1 não encontrado: 1", exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenPlayer2DoesNotExist() {
        Player player1 = createPlayer(1L, "Player 1");
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);

        mockAvailableSchedule();
        mockCategoryExists();

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player1));

        when(playerRepository.findById(2L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto, ADMINISTRATOR_ID));

        assertEquals("Jogador 2 não encontrado: 2", exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenGroupDoesNotExist() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);
        dto.setGroupId(10L);

        mockAvailableSchedule();
        mockCategoryExists();
        mockPlayers(player1, player2);

        when(groupRepository.findByIdAndCategory_Tournament_Administrator_Id(
                10L,
                ADMINISTRATOR_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.createMatch(dto, ADMINISTRATOR_ID));

        assertEquals("Grupo não encontrado: 10", exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldCreateMatchWithGroup() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        com.arenapointhub.api.model.Group group =
                new com.arenapointhub.api.model.Group();

        group.setId(10L);
        group.setName("Grupo A");
        group.setCategory(category);

        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);
        dto.setGroupId(10L);

        mockAvailableSchedule();
        mockCategoryExists();
        mockPlayers(player1, player2);

        when(groupRepository.findByIdAndCategory_Tournament_Administrator_Id(
                10L,
                ADMINISTRATOR_ID))
                .thenReturn(Optional.of(group));

        mockMatchSave();

        MatchResponseDTO response =
                matchService.createMatch(dto, ADMINISTRATOR_ID);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getPlayer1().getId());
        assertEquals(2L, response.getPlayer2().getId());
        assertEquals(MatchStatus.SCHEDULED, response.getStatus());

        verify(groupRepository).findByIdAndCategory_Tournament_Administrator_Id(
                10L,
                ADMINISTRATOR_ID);

        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void shouldReturnMatchById() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(10L, match);

        MatchResponseDTO response =
                matchService.getMatchById(10L, ADMINISTRATOR_ID);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getPlayer1().getId());
        assertEquals(2L, response.getPlayer2().getId());

        verify(matchRepository)
                .findByIdAndCategory_Tournament_Administrator_Id(
                        10L,
                        ADMINISTRATOR_ID);
    }

    @Test
    void shouldReturnAllMatches() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        Match match1 = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        Match match2 = createMatch(
                11L, category, player2, player3,
                MatchPhase.GROUP, MatchStatus.FINISHED, 2, 1);

        when(matchRepository.findByCategory_Tournament_Administrator_Id(
                ADMINISTRATOR_ID))
                .thenReturn(List.of(match1, match2));

        List<MatchResponseDTO> response =
                matchService.getAllMatches(ADMINISTRATOR_ID);

        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals(11L, response.get(1).getId());

        verify(matchRepository)
                .findByCategory_Tournament_Administrator_Id(ADMINISTRATOR_ID);
    }

    @Test
    void shouldReturnMatchesByCategory() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        Match match1 = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        Match match2 = createMatch(
                11L, category, player2, player3,
                MatchPhase.GROUP, MatchStatus.FINISHED, 2, 1);

        when(categoryRepository.existsByIdAndTournament_Administrator_Id(
                CATEGORY_ID,
                ADMINISTRATOR_ID))
                .thenReturn(true);

        when(matchRepository.findByCategoryIdAndCategory_Tournament_Administrator_Id(
                CATEGORY_ID,
                ADMINISTRATOR_ID))
                .thenReturn(List.of(match1, match2));

        List<MatchResponseDTO> response =
                matchService.getMatchesByCategory(
                        CATEGORY_ID,
                        ADMINISTRATOR_ID);

        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals(11L, response.get(1).getId());
        assertEquals(CATEGORY_ID, response.get(0).getCategoryId());
        assertEquals(CATEGORY_ID, response.get(1).getCategoryId());

        verify(matchRepository)
                .findByCategoryIdAndCategory_Tournament_Administrator_Id(
                        CATEGORY_ID,
                        ADMINISTRATOR_ID);
    }

    @Test
    void shouldReturnMatchesByStatus() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        Match match1 = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.FINISHED, 2, 1);

        Match match2 = createMatch(
                11L, category, player2, player3,
                MatchPhase.GROUP, MatchStatus.FINISHED, 2, 0);

        when(matchRepository.findByStatusAndCategory_Tournament_Administrator_Id(
                MatchStatus.FINISHED,
                ADMINISTRATOR_ID))
                .thenReturn(List.of(match1, match2));

        List<MatchResponseDTO> response =
                matchService.getMatchesByStatus(
                        MatchStatus.FINISHED,
                        ADMINISTRATOR_ID);

        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals(11L, response.get(1).getId());
        assertEquals(MatchStatus.FINISHED, response.get(0).getStatus());
        assertEquals(MatchStatus.FINISHED, response.get(1).getStatus());

        verify(matchRepository)
                .findByStatusAndCategory_Tournament_Administrator_Id(
                        MatchStatus.FINISHED,
                        ADMINISTRATOR_ID);
    }

    @Test
    void shouldReturnMatchesByPlayer() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        Match match1 = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.FINISHED, 2, 1);

        Match match2 = createMatch(
                11L, category, player3, player1,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        when(matchRepository.findByPlayerIdAndAdministratorId(
                1L,
                ADMINISTRATOR_ID))
                .thenReturn(List.of(match1, match2));

        List<MatchResponseDTO> response =
                matchService.getMatchesByPlayer(1L, ADMINISTRATOR_ID);

        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals(11L, response.get(1).getId());
        assertEquals(1L, response.get(0).getPlayer1().getId());
        assertEquals(1L, response.get(1).getPlayer2().getId());

        verify(matchRepository)
                .findByPlayerIdAndAdministratorId(1L, ADMINISTRATOR_ID);
    }

    @Test
    void shouldReturnEmptyMatchesByPlayer() {
        when(matchRepository.findByPlayerIdAndAdministratorId(
                1L,
                ADMINISTRATOR_ID))
                .thenReturn(List.of());

        List<MatchResponseDTO> response =
                matchService.getMatchesByPlayer(1L, ADMINISTRATOR_ID);

        assertTrue(response.isEmpty());

        verify(matchRepository)
                .findByPlayerIdAndAdministratorId(1L, ADMINISTRATOR_ID);
    }

    @Test
    void shouldNotReturnMatchWhenIdDoesNotExist() {
        mockMatchLookup(10L, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.getMatchById(10L, ADMINISTRATOR_ID));

        assertEquals(
                "Partida não encontrada com ID: 10",
                exception.getMessage());
    }

    @Test
    void shouldUpdateScore() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.IN_PROGRESS, 1, 0);

        MatchScoreUpdateDTO dto = new MatchScoreUpdateDTO();
        dto.setScorePlayer1(2);
        dto.setScorePlayer2(1);
        dto.setStatus(MatchStatus.FINISHED);

        mockMatchLookup(10L, match);
        mockMatchSave();

        MatchResponseDTO response =
                matchService.updateScore(10L, dto, ADMINISTRATOR_ID);

        assertEquals(2, response.getScorePlayer1());
        assertEquals(1, response.getScorePlayer2());
        assertEquals(MatchStatus.FINISHED, response.getStatus());

        verify(matchRepository).save(match);
    }

    @Test
    void shouldSetChampionWhenFinalIsFinished() {
        Player winner = createPlayer(1L, "Winner");
        Player loser = createPlayer(2L, "Loser");

        Match finalMatch = createMatch(
                30L, category, winner, loser,
                MatchPhase.FINAL, MatchStatus.SCHEDULED, 0, 0);

        MatchScoreUpdateDTO dto = createFinishedScore(2, 0);

        mockMatchLookup(30L, finalMatch);
        mockMatchesByCategory(finalMatch);
        mockMatchSave();

        matchService.updateScore(30L, dto, ADMINISTRATOR_ID);

        assertEquals(winner, category.getChampion());

        verify(categoryRepository).save(category);
    }

    @Test
    void shouldNotUpdateScoreWhenMatchDoesNotExist() {
        MatchScoreUpdateDTO dto = createFinishedScore(2, 1);

        mockMatchLookup(10L, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.updateScore(10L, dto, ADMINISTRATOR_ID));

        assertEquals(
                "Partida não encontrada com ID: 10",
                exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotSaveMatchSetsWhenMatchDoesNotExist() {
        MatchScoreRequestDTO dto = new MatchScoreRequestDTO();
        dto.setSets(List.of(new MatchSetDTO(1, 11, 5)));

        mockMatchLookup(10L, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.saveMatchSets(
                        10L,
                        dto,
                        ADMINISTRATOR_ID));

        assertEquals(
                "Partida não encontrada com ID: 10",
                exception.getMessage());

        verify(matchSetRepository, never()).save(any(MatchSet.class));
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldSaveValidSetsAndFinishMatch() {
        category.setSetsToWinMatch(2);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.IN_PROGRESS, 0, 0);

        MatchScoreRequestDTO dto = new MatchScoreRequestDTO();
        dto.setSets(List.of(
                new MatchSetDTO(1, 11, 5),
                new MatchSetDTO(2, 11, 7)));

        mockMatchLookup(10L, match);
        mockNewSet(10L, 1);
        mockNewSet(10L, 2);

        when(matchSetRepository.findByMatchId(10L))
                .thenReturn(List.of(
                        createMatchSet(match, 1, 11, 5),
                        createMatchSet(match, 2, 11, 7)));

        mockMatchSave();

        matchService.saveMatchSets(10L, dto, ADMINISTRATOR_ID);

        assertEquals(2, match.getScorePlayer1());
        assertEquals(0, match.getScorePlayer2());
        assertEquals(MatchStatus.FINISHED, match.getStatus());

        verify(matchSetRepository, times(2)).save(any(MatchSet.class));
        verify(matchRepository).save(match);
    }

    @Test
    void shouldKeepMatchInProgressWhenSetsAreNotEnoughToWin() {
        category.setSetsToWinMatch(2);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.IN_PROGRESS, 0, 0);

        MatchScoreRequestDTO dto = new MatchScoreRequestDTO();
        dto.setSets(List.of(new MatchSetDTO(1, 11, 5)));

        mockMatchLookup(10L, match);
        mockNewSet(10L, 1);

        when(matchSetRepository.findByMatchId(10L))
                .thenReturn(List.of(createMatchSet(match, 1, 11, 5)));

        mockMatchSave();

        matchService.saveMatchSets(10L, dto, ADMINISTRATOR_ID);

        assertEquals(1, match.getScorePlayer1());
        assertEquals(0, match.getScorePlayer2());
        assertEquals(MatchStatus.IN_PROGRESS, match.getStatus());

        verify(matchSetRepository).save(any(MatchSet.class));
        verify(matchRepository).save(match);
    }

    @Test
    void shouldNotSaveInvalidSetScore() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.IN_PROGRESS, 0, 0);

        MatchScoreRequestDTO dto = new MatchScoreRequestDTO();
        dto.setSets(List.of(new MatchSetDTO(1, 10, 8)));

        mockMatchLookup(10L, match);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.saveMatchSets(
                        10L,
                        dto,
                        ADMINISTRATOR_ID));

        assertTrue(exception.getMessage().contains(
                "Placar inválido para o set 1"));

        verify(matchSetRepository, never()).save(any(MatchSet.class));
    }

    @Test
    void shouldAcceptValidDeuceSetScore() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.IN_PROGRESS, 0, 0);

        MatchScoreRequestDTO dto = new MatchScoreRequestDTO();
        dto.setSets(List.of(new MatchSetDTO(1, 12, 10)));

        mockMatchLookup(10L, match);
        mockNewSet(10L, 1);

        when(matchSetRepository.findByMatchId(10L))
                .thenReturn(List.of(createMatchSet(match, 1, 12, 10)));

        mockMatchSave();

        matchService.saveMatchSets(10L, dto, ADMINISTRATOR_ID);

        assertEquals(1, match.getScorePlayer1());
        assertEquals(0, match.getScorePlayer2());
        assertEquals(MatchStatus.IN_PROGRESS, match.getStatus());

        verify(matchSetRepository).save(any(MatchSet.class));
        verify(matchRepository).save(match);
    }

    @Test
    void shouldReturnSetsByMatchId() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.FINISHED, 2, 0);

        mockMatchLookup(10L, match);

        when(matchSetRepository.findByMatchId(10L))
                .thenReturn(List.of(
                        createMatchSet(match, 1, 11, 5),
                        createMatchSet(match, 2, 11, 7)));

        List<MatchSetDTO> response =
                matchService.getSetsByMatchId(10L, ADMINISTRATOR_ID);

        assertEquals(2, response.size());
        assertEquals(1, response.get(0).getSetNumber());
        assertEquals(11, response.get(0).getScorePlayer1());
        assertEquals(5, response.get(0).getScorePlayer2());
    }

    @Test
    void shouldRegisterWalkoverForPlayer1() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(1L);

        mockMatchLookup(10L, match);
        mockMatchSave();

        MatchResponseDTO response =
                matchService.registerWalkover(10L, dto, ADMINISTRATOR_ID);

        assertEquals(MatchStatus.WO, response.getStatus());
        assertEquals(2, response.getScorePlayer1());
        assertEquals(0, response.getScorePlayer2());

        verify(matchRepository).save(match);
        verify(matchSetRepository, times(2)).save(any(MatchSet.class));
    }

    @Test
    void shouldNotRegisterWalkoverForPlayerOutsideMatch() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(3L);

        mockMatchLookup(10L, match);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.registerWalkover(
                        10L,
                        dto,
                        ADMINISTRATOR_ID));

        assertEquals(
                "O jogador informado não faz parte desta partida.",
                exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldUpdateMatch() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);
        dto.setScorePlayer1(2);
        dto.setScorePlayer2(1);
        dto.setStatus(MatchStatus.FINISHED);

        mockMatchLookup(10L, match);
        mockMatchSave();

        MatchResponseDTO response =
                matchService.updateMatch(10L, dto, ADMINISTRATOR_ID);

        assertEquals(2, response.getScorePlayer1());
        assertEquals(1, response.getScorePlayer2());
        assertEquals(MatchStatus.FINISHED, response.getStatus());

        verify(matchRepository).save(match);
    }

    @Test
    void shouldNotUpdateMatchWhenMatchDoesNotExist() {
        MatchRequestDTO dto = createMatchRequest(CATEGORY_ID, 1L, 2L);
        dto.setScorePlayer1(2);
        dto.setScorePlayer2(1);
        dto.setStatus(MatchStatus.FINISHED);

        mockMatchLookup(10L, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> matchService.updateMatch(
                        10L,
                        dto,
                        ADMINISTRATOR_ID));

        assertEquals(
                "Partida não encontrada com ID: 10",
                exception.getMessage());

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldAdvanceWinnerFromQuarterFinalToSemiFinal() {
        Player winner = createPlayer(1L, "Winner");
        Player opponent = createPlayer(2L, "Opponent");
        Player other1 = createPlayer(3L, "Other 1");
        Player other2 = createPlayer(4L, "Other 2");

        Match quarter1 = createMatch(
                10L, category, winner, opponent,
                MatchPhase.QUARTER_FINAL, MatchStatus.IN_PROGRESS, 3, 1);

        Match quarter2 = createMatch(
                11L, category, other1, other2,
                MatchPhase.QUARTER_FINAL, MatchStatus.SCHEDULED, 0, 0);

        Match semiFinal = createMatch(
                20L, category, null, null,
                MatchPhase.SEMI_FINAL, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(10L, quarter1);
        mockMatchesByCategory(quarter1, quarter2, semiFinal);
        mockMatchSave();

        MatchScoreUpdateDTO dto = createFinishedScore(2, 0);

        matchService.updateScore(10L, dto, ADMINISTRATOR_ID);

        assertEquals(winner, semiFinal.getPlayer1());

        verify(matchRepository).save(semiFinal);
    }

    @Test
    void shouldAdvanceByeWinnerFromQuarterFinalToSemiFinal() {
        Player byeWinner = createPlayer(1L, "Bye Winner");

        Match quarterFinal = createMatch(
                10L, category, byeWinner, null,
                MatchPhase.QUARTER_FINAL, MatchStatus.IN_PROGRESS, 0, 0);

        Match semiFinal = createMatch(
                20L, category, null, null,
                MatchPhase.SEMI_FINAL, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(10L, quarterFinal);
        mockMatchesByCategory(quarterFinal, semiFinal);
        mockMatchSave();

        MatchScoreUpdateDTO dto = createFinishedScore(2, 0);

        matchService.updateScore(10L, dto, ADMINISTRATOR_ID);

        assertEquals(byeWinner, semiFinal.getPlayer1());

        verify(matchRepository).save(semiFinal);
    }

    @Test
    void shouldAdvanceWinnerFromSecondQuarterFinalToSemiFinalPlayer2() {
        Player player1 = createPlayer(1L, "Player 1");
        Player winner = createPlayer(3L, "Winner");
        Player opponent = createPlayer(4L, "Opponent");

        Match quarter1 = createMatch(
                10L, category, player1, createPlayer(2L, "Player 2"),
                MatchPhase.QUARTER_FINAL, MatchStatus.IN_PROGRESS, 3, 1);

        Match quarter2 = createMatch(
                11L, category, opponent, winner,
                MatchPhase.QUARTER_FINAL, MatchStatus.IN_PROGRESS, 3, 1);

        Match semiFinal = createMatch(
                20L, category, null, null,
                MatchPhase.SEMI_FINAL, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(11L, quarter2);
        mockMatchesByCategory(quarter1, quarter2, semiFinal);
        mockMatchSave();

        MatchScoreUpdateDTO dto = createFinishedScore(0, 2);

        matchService.updateScore(11L, dto, ADMINISTRATOR_ID);

        assertEquals(winner, semiFinal.getPlayer2());

        verify(matchRepository).save(semiFinal);
    }

    @Test
    void shouldAdvanceWinnerFromSemiFinalToFinal() {
        Player winner = createPlayer(1L, "Winner");
        Player opponent = createPlayer(2L, "Opponent");
        Player otherWinner = createPlayer(3L, "Other Winner");

        Match semiFinal1 = createMatch(
                20L, category, winner, opponent,
                MatchPhase.SEMI_FINAL, MatchStatus.IN_PROGRESS, 2, 0);

        Match semiFinal2 = createMatch(
                21L, category, otherWinner, null,
                MatchPhase.SEMI_FINAL, MatchStatus.SCHEDULED, 0, 0);

        Match finalMatch = createMatch(
                30L, category, null, null,
                MatchPhase.FINAL, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(20L, semiFinal1);
        mockMatchesByCategory(semiFinal1, semiFinal2, finalMatch);
        mockMatchSave();

        MatchScoreUpdateDTO dto = createFinishedScore(2, 0);

        matchService.updateScore(20L, dto, ADMINISTRATOR_ID);

        assertEquals(winner, finalMatch.getPlayer1());

        verify(matchRepository).save(finalMatch);
    }

    @Test
    void shouldAdvanceWinnerFromSecondSemiFinalToFinalPlayer2() {
        Player otherWinner = createPlayer(1L, "Other Winner");
        Player winner = createPlayer(3L, "Winner");
        Player opponent = createPlayer(4L, "Opponent");

        Match semiFinal1 = createMatch(
                20L, category, otherWinner, createPlayer(2L, "Other Opponent"),
                MatchPhase.SEMI_FINAL, MatchStatus.IN_PROGRESS, 2, 0);

        Match semiFinal2 = createMatch(
                21L, category, winner, opponent,
                MatchPhase.SEMI_FINAL, MatchStatus.IN_PROGRESS, 2, 0);

        Match finalMatch = createMatch(
                30L, category, null, null,
                MatchPhase.FINAL, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(21L, semiFinal2);
        mockMatchesByCategory(semiFinal1, semiFinal2, finalMatch);
        mockMatchSave();

        MatchScoreUpdateDTO dto = createFinishedScore(2, 0);

        matchService.updateScore(21L, dto, ADMINISTRATOR_ID);

        assertEquals(winner, finalMatch.getPlayer2());

        verify(matchRepository).save(finalMatch);
    }

    @Test
    void shouldAdvanceWalkoverWinnerFromSemiFinalToFinal() {
        Player winner = createPlayer(1L, "Winner");
        Player opponent = createPlayer(2L, "Opponent");
        Player otherWinner = createPlayer(3L, "Other Winner");

        Match semiFinal1 = createMatch(
                20L, category, winner, opponent,
                MatchPhase.SEMI_FINAL, MatchStatus.SCHEDULED, 0, 0);

        Match semiFinal2 = createMatch(
                21L, category, otherWinner, null,
                MatchPhase.SEMI_FINAL, MatchStatus.SCHEDULED, 0, 0);

        Match finalMatch = createMatch(
                30L, category, null, null,
                MatchPhase.FINAL, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(20L, semiFinal1);
        mockMatchesByCategory(semiFinal1, semiFinal2, finalMatch);
        mockMatchSave();

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(1L);

        MatchResponseDTO response =
                matchService.registerWalkover(20L, dto, ADMINISTRATOR_ID);

        assertEquals(MatchStatus.WO, response.getStatus());
        assertEquals(2, response.getScorePlayer1());
        assertEquals(0, response.getScorePlayer2());
        assertEquals(winner, finalMatch.getPlayer1());

        verify(matchRepository).save(finalMatch);
    }

    @Test
    void shouldDistributeTournamentPointsWhenFinalIsFinished() {
        Player winner = createPlayer(1L, "Winner");
        Player loser = createPlayer(2L, "Loser");

        Match finalMatch = createMatch(
                30L, category, winner, loser,
                MatchPhase.FINAL, MatchStatus.SCHEDULED, 0, 0);

        mockMatchLookup(30L, finalMatch);
        mockMatchesByCategory(finalMatch);
        mockMatchSave();

        MatchScoreUpdateDTO dto = createFinishedScore(2, 0);

        matchService.updateScore(30L, dto, ADMINISTRATOR_ID);

        verify(globalRankingService).distributeTournamentPoints(
                any(Tournament.class),
                any(Category.class),
                any());
    }

    @Test
    void shouldNotRegisterWalkoverWhenWinnerIsNull() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.SCHEDULED, 0, 0);

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(null);

        mockMatchLookup(10L, match);

        assertThrows(
                BusinessException.class,
                () -> matchService.registerWalkover(
                        10L,
                        dto,
                        ADMINISTRATOR_ID));

        verify(matchRepository, never()).save(any(Match.class));
        verify(matchSetRepository, never()).save(any(MatchSet.class));
    }

    @Test
    void shouldNotRegisterWalkoverForFinishedMatch() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.FINISHED, 2, 0);

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(1L);

        mockMatchLookup(10L, match);

        assertThrows(
                BusinessException.class,
                () -> matchService.registerWalkover(
                        10L,
                        dto,
                        ADMINISTRATOR_ID));

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotRegisterWalkoverForMatchAlreadyMarkedAsWo() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");

        Match match = createMatch(
                10L, category, player1, player2,
                MatchPhase.GROUP, MatchStatus.WO, 2, 0);

        MatchWoRequestDTO dto = new MatchWoRequestDTO();
        dto.setWinnerPlayerId(1L);

        mockMatchLookup(10L, match);

        assertThrows(
                BusinessException.class,
                () -> matchService.registerWalkover(
                        10L,
                        dto,
                        ADMINISTRATOR_ID));

        verify(matchRepository, never()).save(any(Match.class));
    }

    private void mockAvailableSchedule() {
        when(matchRepository.existsByTableOrCourtAndScheduledTimeAndStatusNot(
                "Mesa 1",
                "10:00",
                MatchStatus.CANCELED))
                .thenReturn(false);

        when(matchRepository.existsByPlayerBusy(
                1L,
                2L,
                "10:00",
                MatchStatus.CANCELED))
                .thenReturn(false);
    }

    private void mockCategoryExists() {
        when(categoryRepository.findByIdAndTournament_Administrator_Id(
                CATEGORY_ID,
                ADMINISTRATOR_ID))
                .thenReturn(Optional.of(category));
    }

    private void mockPlayers(Player player1, Player player2) {
        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player1));

        when(playerRepository.findById(2L))
                .thenReturn(Optional.of(player2));
    }

    private void mockMatchLookup(Long matchId, Match match) {
        when(matchRepository.findByIdAndCategory_Tournament_Administrator_Id(
                matchId,
                ADMINISTRATOR_ID))
                .thenReturn(Optional.ofNullable(match));
    }

    private void mockMatchesByCategory(Match... matches) {
        when(matchRepository.findByCategoryIdAndCategory_Tournament_Administrator_Id(
                CATEGORY_ID,
                ADMINISTRATOR_ID))
                .thenReturn(List.of(matches));
    }

    private void mockMatchSave() {
        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> {
                    Match match = invocation.getArgument(0);

                    if (match.getId() == null) {
                        match.setId(10L);
                    }

                    return match;
                });
    }

    private void mockNewSet(Long matchId, int setNumber) {
        when(matchSetRepository.findByMatchIdAndSetNumber(
                matchId,
                setNumber))
                .thenReturn(Optional.empty());
    }

    private MatchScoreUpdateDTO createFinishedScore(
            int scorePlayer1,
            int scorePlayer2) {

        MatchScoreUpdateDTO dto = new MatchScoreUpdateDTO();
        dto.setScorePlayer1(scorePlayer1);
        dto.setScorePlayer2(scorePlayer2);
        dto.setStatus(MatchStatus.FINISHED);

        return dto;
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
        Category testCategory = new Category();
        testCategory.setId(id);
        testCategory.setName("Sub 15");
        testCategory.setSetsToWinMatch(2);

        Administrator administrator = new Administrator();
        administrator.setId(ADMINISTRATOR_ID);

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Tournament Test");
        tournament.setAdministrator(administrator);

        testCategory.setTournament(tournament);

        return testCategory;
    }

    private Player createPlayer(Long id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setEmail(
                name.toLowerCase().replace(" ", "") + "@email.com");
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
