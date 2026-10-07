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
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
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
        		bracketService.generateKnockoutBracket(1L);

        assertEquals(1L, response.getCategoryId());
        assertEquals("Sub 15", response.getCategoryName());
        assertEquals(4, response.getTargetBracketSize());
        assertEquals(3, response.getMatches().size());

        org.mockito.ArgumentCaptor<Match> matchCaptor =
                org.mockito.ArgumentCaptor.forClass(Match.class);

        verify(matchRepository, org.mockito.Mockito.times(3))
        		.save(matchCaptor.capture());

        List<Match> savedMatches = matchCaptor.getAllValues();

        assertEquals(MatchPhase.SEMI_FINAL, savedMatches.get(0).getPhase());
        assertEquals(MatchPhase.SEMI_FINAL, savedMatches.get(1).getPhase());
        assertEquals(MatchPhase.FINAL, savedMatches.get(2).getPhase());
    }
    
    @Test
    void shouldDistributeByesAcrossDifferentSemifinals() {
        Category category = createCategory(1L);

        Group groupA = createGroup(1L, "Grupo A", category);
        Group groupB = createGroup(2L, "Grupo B", category);
        Group groupC = createGroup(3L, "Grupo C", category);

        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");
        Player player4 = createPlayer(4L, "Player 4");
        Player player5 = createPlayer(5L, "Player 5");
        Player player6 = createPlayer(6L, "Player 6");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(new ArrayList<>(
                        List.of(groupA, groupB, groupC)));

        when(groupService.calculateGroupStandings(1L))
                .thenReturn(List.of(
                        createStanding(player1),
                        createStanding(player2)));

        when(groupService.calculateGroupStandings(2L))
                .thenReturn(List.of(
                        createStanding(player3),
                        createStanding(player4)));

        when(groupService.calculateGroupStandings(3L))
                .thenReturn(List.of(
                        createStanding(player5),
                        createStanding(player6)));

        when(playerRepository.findById(1L))
                .thenReturn(Optional.of(player1));
        when(playerRepository.findById(2L))
                .thenReturn(Optional.of(player2));
        when(playerRepository.findById(3L))
                .thenReturn(Optional.of(player3));
        when(playerRepository.findById(4L))
                .thenReturn(Optional.of(player4));
        when(playerRepository.findById(5L))
                .thenReturn(Optional.of(player5));
        when(playerRepository.findById(6L))
                .thenReturn(Optional.of(player6));

        long[] nextId = {100L};

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> {
                    Match match = invocation.getArgument(0);
                    match.setId(nextId[0]++);
                    return match;
                });

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(1L);

        assertEquals(7, response.getMatches().size());

        org.mockito.ArgumentCaptor<Match> matchCaptor =
                org.mockito.ArgumentCaptor.forClass(Match.class);

        verify(matchRepository, org.mockito.Mockito.times(9))
        		.save(matchCaptor.capture());

        List<Match> matches = matchCaptor.getAllValues();

        // Primeira fase: 4 partidas
        assertEquals(MatchPhase.QUARTER_FINAL,
                matches.get(0).getPhase());

        assertEquals(MatchStatus.FINISHED,
                matches.get(0).getStatus());

        assertEquals(1L,
                matches.get(0).getPlayer1().getId());

        assertEquals(null, matches.get(0).getPlayer2());

        assertEquals(MatchStatus.FINISHED,
                matches.get(2).getStatus());

        assertEquals(3L,
                matches.get(2).getPlayer1().getId());

        assertEquals(null, matches.get(2).getPlayer2());

        // Semifinais: cada BYE deve ocupar uma semifinal diferente
        assertEquals(MatchPhase.SEMI_FINAL,
                matches.get(4).getPhase());

        assertEquals(MatchPhase.SEMI_FINAL,
                matches.get(5).getPhase());

        assertEquals(1L,
                matches.get(4).getPlayer1().getId());

        assertEquals(3L,
                matches.get(5).getPlayer1().getId());
    }

    @Test
    void shouldGenerateCorrectSeedingForEightGroups() {

        Category category = createCategory(1L);

        List<Group> groups = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            groups.add(
                    createGroup(
                            (long) i,
                            "Grupo " + (char) ('A' + i - 1),
                            category
                    )
            );
        }

        List<Player> firstPlaces = new ArrayList<>();
        List<Player> secondPlaces = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {

            Player first =
                    createPlayer(
                            (long) (i * 2 - 1),
                            "1." + i
                    );

            Player second =
                    createPlayer(
                            (long) (i * 2),
                            "2." + i
                    );

            firstPlaces.add(first);
            secondPlaces.add(second);

            when(playerRepository.findById(first.getId()))
                    .thenReturn(Optional.of(first));

            when(playerRepository.findById(second.getId()))
                    .thenReturn(Optional.of(second));

            when(groupService.calculateGroupStandings((long) i))
                    .thenReturn(List.of(
                            createStanding(first),
                            createStanding(second)
                    ));
        }

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(groupRepository.findByCategoryId(1L))
                .thenReturn(new ArrayList<>(groups));

        long[] nextId = {100L};

        when(matchRepository.save(any(Match.class)))
                .thenAnswer(invocation -> {
                    Match match = invocation.getArgument(0);
                    match.setId(nextId[0]++);
                    return match;
                });

        BracketResponseDTO response =
                bracketService.generateKnockoutBracket(1L);

        assertEquals(16, response.getTargetBracketSize());
        assertEquals(15, response.getMatches().size());

        org.mockito.ArgumentCaptor<Match> matchCaptor =
                org.mockito.ArgumentCaptor.forClass(Match.class);

        verify(matchRepository, org.mockito.Mockito.times(15))
                .save(matchCaptor.capture());

        List<Match> matches =
                matchCaptor.getAllValues();

        // Q1 → 1.1 × 2.7
        assertEquals(1L, matches.get(0).getPlayer1().getId());
        assertEquals(14L, matches.get(0).getPlayer2().getId());

        // Q2 → 1.8 × 2.5
        assertEquals(15L, matches.get(1).getPlayer1().getId());
        assertEquals(10L, matches.get(1).getPlayer2().getId());

        // Q3 → 1.6 × 2.2
        assertEquals(11L, matches.get(2).getPlayer1().getId());
        assertEquals(4L, matches.get(2).getPlayer2().getId());

        // Q4 → 1.4 × 2.3
        assertEquals(7L, matches.get(3).getPlayer1().getId());
        assertEquals(6L, matches.get(3).getPlayer2().getId());

        // Q5 → 1.3 × 2.6
        assertEquals(5L, matches.get(4).getPlayer1().getId());
        assertEquals(12L, matches.get(4).getPlayer2().getId());

        // Q6 → 1.5 × 2.1
        assertEquals(9L, matches.get(5).getPlayer1().getId());
        assertEquals(2L, matches.get(5).getPlayer2().getId());

        // Q7 → 1.7 × 2.4
        assertEquals(13L, matches.get(6).getPlayer1().getId());
        assertEquals(8L, matches.get(6).getPlayer2().getId());

        // Q8 → 1.2 × 2.8
        assertEquals(3L, matches.get(7).getPlayer1().getId());
        assertEquals(16L, matches.get(7).getPlayer2().getId());
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