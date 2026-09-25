package com.arenapointhub.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arenapointhub.api.dto.BracketResponseDTO;
import com.arenapointhub.api.dto.GroupStandingDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;

class BracketServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private BracketService bracketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        bracketService = new BracketService(
                groupRepository,
                groupService,
                matchRepository,
                playerRepository,
                categoryRepository
        );
    }

    @Test
    void shouldReturnOrderedGroupWinners() {
        Category category = createCategory(1L);

        Group groupB = createGroup(2L, "Grupo B", category);
        Group groupA = createGroup(1L, "Grupo A", category);

        Player playerA = createPlayer(1L, "Player A");
        Player playerB = createPlayer(2L, "Player B");

        GroupStandingDTO standingA = createStanding(playerA);
        GroupStandingDTO standingB = createStanding(playerB);

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(new ArrayList<>(List.of(groupB, groupA)));

        when(groupService.calculateGroupStandings(1L))
                .thenReturn(List.of(standingA));

        when(groupService.calculateGroupStandings(2L))
                .thenReturn(List.of(standingB));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(playerA));

        when(playerRepository.findById(2L))
                .thenReturn(Optional.of(playerB));

        List<Player> winners =
                bracketService.getOrderedGroupWinners(1L);

        assertEquals(2, winners.size());
        assertEquals(1L, winners.get(0).getId());
        assertEquals(2L, winners.get(1).getId());

        verify(groupRepository).findByCategoryId(1L);
    }

    @Test
    void shouldReturnFirstPlacePlayersBeforeSecondPlacePlayers() {
        Category category = createCategory(1L);

        Group groupA = createGroup(1L, "Grupo A", category);
        Group groupB = createGroup(2L, "Grupo B", category);

        Player firstA = createPlayer(1L, "First A");
        Player secondA = createPlayer(2L, "Second A");
        Player firstB = createPlayer(3L, "First B");
        Player secondB = createPlayer(4L, "Second B");

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(new ArrayList<>(List.of(groupB, groupA)));

        when(groupService.calculateGroupStandings(1L))
                .thenReturn(List.of(
                        createStanding(firstA),
                        createStanding(secondA)
                ));

        when(groupService.calculateGroupStandings(2L))
                .thenReturn(List.of(
                        createStanding(firstB),
                        createStanding(secondB)
                ));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(firstA));

        when(playerRepository.findById(2L))
                .thenReturn(Optional.of(secondA));

        when(playerRepository.findById(3L))
                .thenReturn(Optional.of(firstB));

        when(playerRepository.findById(4L))
                .thenReturn(Optional.of(secondB));

        List<Player> players =
                bracketService.getOrderedQualifiedPlayers(1L);

        assertEquals(4, players.size());

        assertEquals(1L, players.get(0).getId());
        assertEquals(3L, players.get(1).getId());
        assertEquals(2L, players.get(2).getId());
        assertEquals(4L, players.get(3).getId());
    }

    @Test
    void shouldCalculateRequiredByes() {
        assertEquals(
                2,
                bracketService.calculateRequiredByes(6, 8)
        );
    }

    @Test
    void shouldNotCalculateByesWhenQualifiedPlayersExceedBracketSize() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.calculateRequiredByes(9, 8)
        );

        assertEquals(
                "O número de classificados não pode ser maior que o tamanho da chave.",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotReturnGroupWinnersWhenCategoryHasNoGroups() {
        when(groupRepository.findByCategoryId(1L))
                .thenReturn(new ArrayList<>());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bracketService.getOrderedGroupWinners(1L)
        );

        assertEquals(
                "Não existem grupos para esta categoria.",
                exception.getMessage()
        );
    }

    @Test
    void shouldGenerateFirstRoundKnockoutBracket() {
        Category category = createCategory(1L);

        Group groupA = createGroup(1L, "Grupo A", category);
        Group groupB = createGroup(2L, "Grupo B", category);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");
        Player player4 = createPlayer(4L, "Player 4");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(new ArrayList<>(List.of(groupA, groupB)));

        when(groupService.calculateGroupStandings(1L))
                .thenReturn(List.of(
                        createStanding(player1),
                        createStanding(player2)
                ));

        when(groupService.calculateGroupStandings(2L))
                .thenReturn(List.of(
                        createStanding(player3),
                        createStanding(player4)
                ));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player1));

        when(playerRepository.findById(2L))
                .thenReturn(Optional.of(player2));

        when(playerRepository.findById(3L))
                .thenReturn(Optional.of(player3));

        when(playerRepository.findById(4L))
                .thenReturn(Optional.of(player4));

        when(matchRepository.save(any(Match.class)))
        .thenAnswer(invocation -> {
            Match match = invocation.getArgument(0);
            if (match.getId() == null) {
                match.setId(100L);
            }
            return match;
        });

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(1L, 4);

        assertEquals(1L, response.getCategoryId());
        assertEquals("Sub 15", response.getCategoryName());
        assertEquals(4, response.getTargetBracketSize());
        assertEquals(2, response.getMatches().size());

        verify(matchRepository, org.mockito.Mockito.times(2))
                .save(any(Match.class));
    }

    private Category createCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Sub 15");
        return category;
    }

    private Group createGroup(
            Long id,
            String name,
            Category category) {

        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setCategory(category);
        group.setPlayers(new ArrayList<>());

        return group;
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

    private GroupStandingDTO createStanding(Player player) {
        GroupStandingDTO standing = new GroupStandingDTO();
        standing.setPlayerId(player.getId());
        standing.setPlayerName(player.getName());
        standing.setClubAcademy(player.getClubAcademy());

        return standing;
    }
}