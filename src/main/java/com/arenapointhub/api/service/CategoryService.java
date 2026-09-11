package com.arenapointhub.api.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.CategoryRequestDTO;
import com.arenapointhub.api.dto.CategoryResponseDTO;
import com.arenapointhub.api.dto.MatchResponseDTO;
import com.arenapointhub.api.dto.PlayerSummaryDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Match;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.model.enums.CategoryType;
import com.arenapointhub.api.model.enums.MatchPhase;
import com.arenapointhub.api.model.enums.MatchStatus;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.MatchRepository;
import com.arenapointhub.api.repository.PlayerRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    // Injeção de dependência via construtor (sem @Autowired)
    public CategoryService(CategoryRepository categoryRepository, 
                           TournamentRepository tournamentRepository,
                           MatchRepository matchRepository,
                           PlayerRepository playerRepository) {
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new BusinessException("Torneio não encontrado com o ID: " + dto.getTournamentId()));

        Category category = new Category();
        category.setTournament(tournament);
        copyDtoToEntity(dto, category);

        Category savedCategory = categoryRepository.save(category);
        return mapToResponseDTO(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com o ID: " + id));
        return mapToResponseDTO(category);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com o ID: " + id));

        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new BusinessException("Torneio não encontrado com o ID: " + dto.getTournamentId()));

        category.setTournament(tournament);
        copyDtoToEntity(dto, category);

        Category updatedCategory = categoryRepository.save(category);
        return mapToResponseDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException("Categoria não encontrada com o ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private void copyDtoToEntity(CategoryRequestDTO dto, Category category) {
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setMinAge(dto.getMinAge());
        category.setMaxAge(dto.getMaxAge());
        category.setDescription(dto.getDescription());
        category.setScoringSystem(dto.getScoringSystem());
        category.setSetsToWinMatch(dto.getSetsToWinMatch()); // Adicionado aqui
    }

    private CategoryResponseDTO mapToResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setMinAge(category.getMinAge());
        dto.setMaxAge(category.getMaxAge());
        dto.setDescription(category.getDescription());
        dto.setScoringSystem(category.getScoringSystem());
        dto.setSetsToWinMatch(category.getSetsToWinMatch()); // Adicionado aqui

        if (category.getTournament() != null) {
            dto.setTournamentId(category.getTournament().getId());
        }

        return dto;
    }
    
    @Transactional
    public void generatePlayoffs(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com ID: " + categoryId));

        List<Group> groups = category.getGroups();
        if (groups == null || groups.isEmpty()) {
            throw new BusinessException("A categoria não possui grupos cadastrados.");
        }

        List<Player> qualifiedPlayers = new ArrayList<>();

        for (Group group : groups) {
            List<Match> groupMatches = matchRepository.findByGroupId(group.getId());
            
            Map<Player, Integer> playerWins = new HashMap<>();
            
            for (Match m : groupMatches) {
                if (m.getPlayer1() != null) playerWins.putIfAbsent(m.getPlayer1(), 0);
                if (m.getPlayer2() != null) playerWins.putIfAbsent(m.getPlayer2(), 0);
            }

            for (Match m : groupMatches) {
                if (m.getStatus() == MatchStatus.FINISHED) {
                    if (m.getScorePlayer1() > m.getScorePlayer2()) {
                        playerWins.put(m.getPlayer1(), playerWins.get(m.getPlayer1()) + 1);
                    } else if (m.getScorePlayer2() > m.getScorePlayer1()) {
                        playerWins.put(m.getPlayer2(), playerWins.get(m.getPlayer2()) + 1);
                    }
                }
            }

            List<Player> sortedGroupPlayers = playerWins.entrySet().stream()
                    .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (sortedGroupPlayers.size() < 2) {
                throw new BusinessException("O grupo precisa ter pelo menos 2 jogadores computados.");
            }

            qualifiedPlayers.add(sortedGroupPlayers.get(0));
            qualifiedPlayers.add(sortedGroupPlayers.get(1));
        }

        // Limpa playoffs anteriores
        matchRepository.deleteByCategoryIdAndPhaseNot(categoryId, MatchPhase.GROUP);

        if (qualifiedPlayers.size() < 8) {
            throw new BusinessException("São necessários pelo menos 8 jogadores classificados para gerar os playoffs.");
        }

        List<Match> quarters = new ArrayList<>();
        int[][] pairings = { {0, 7}, {3, 4}, {1, 6}, {2, 5} };

        for (int[] pair : pairings) {
            Match match = new Match();
            match.setCategory(category);
            match.setPlayer1(qualifiedPlayers.get(pair[0]));
            match.setPlayer2(qualifiedPlayers.get(pair[1]));
            match.setPhase(MatchPhase.QUARTER_FINAL);
            match.setStatus(MatchStatus.SCHEDULED);
            match.setScorePlayer1(0);
            match.setScorePlayer2(0);
            quarters.add(matchRepository.save(match));
        }

        Match semi1 = new Match();
        semi1.setCategory(category);
        semi1.setPhase(MatchPhase.SEMI_FINAL);
        semi1.setStatus(MatchStatus.SCHEDULED);
        semi1.setScorePlayer1(0);
        semi1.setScorePlayer2(0);
        matchRepository.save(semi1);

        Match semi2 = new Match();
        semi2.setCategory(category);
        semi2.setPhase(MatchPhase.SEMI_FINAL);
        semi2.setStatus(MatchStatus.SCHEDULED);
        semi2.setScorePlayer1(0);
        semi2.setScorePlayer2(0);
        matchRepository.save(semi2);

        Match finalMatch = new Match();
        finalMatch.setCategory(category);
        finalMatch.setPhase(MatchPhase.FINAL);
        finalMatch.setStatus(MatchStatus.SCHEDULED);
        finalMatch.setScorePlayer1(0);
        finalMatch.setScorePlayer2(0);
        matchRepository.save(finalMatch);
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException("Categoria não encontrada com ID: " + categoryId);
        }

        return matchRepository.findByCategoryId(categoryId).stream()
                .map(this::mapMatchToResponseDTO)
                .collect(Collectors.toList());
    }

    private MatchResponseDTO mapMatchToResponseDTO(Match match) {
        MatchResponseDTO dto = new MatchResponseDTO();
        dto.setId(match.getId());
        
        if (match.getCategory() != null) {
            dto.setCategoryId(match.getCategory().getId());
            dto.setCategoryName(match.getCategory().getName());
        }

        if (match.getPlayer1() != null) {
            PlayerSummaryDTO p1 = new PlayerSummaryDTO();
            p1.setId(match.getPlayer1().getId());
            p1.setName(match.getPlayer1().getName());
            dto.setPlayer1(p1);
        }

        if (match.getPlayer2() != null) {
            PlayerSummaryDTO p2 = new PlayerSummaryDTO();
            p2.setId(match.getPlayer2().getId());
            p2.setName(match.getPlayer2().getName());
            dto.setPlayer2(p2);
        }

        dto.setTableOrCourt(match.getTableOrCourt());
        dto.setScheduledTime(match.getScheduledTime());
        dto.setScorePlayer1(match.getScorePlayer1());
        dto.setScorePlayer2(match.getScorePlayer2());
        dto.setStatus(match.getStatus());
        dto.setPhase(match.getPhase()); 

        return dto;
    }
    
    @Transactional(readOnly = true)
    public Player getChampionByCategoryId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com ID: " + categoryId));
        return category.getChampion();
    }
    
    public void validatePlayerAgeForCategory(Player player, Category category) {
        if (category.getType() == CategoryType.AGE) {
            if (player.getBirthDate() == null) {
                throw new BusinessException("A data de nascimento do jogador é obrigatória para categorias restritas por idade.");
            }

            int currentYear = LocalDate.now().getYear();
            int birthYear = player.getBirthDate().getYear();
            int age = currentYear - birthYear;

            if (category.getMaxAge() != null && age > category.getMaxAge()) {
                throw new BusinessException("O jogador tem " + age + " anos e excede a idade máxima de " + category.getMaxAge() + " anos para a categoria " + category.getName());
            }

            if (category.getMinAge() != null && age < category.getMinAge()) {
                throw new BusinessException("O jogador tem " + age + " anos e está abaixo da idade mínima de " + category.getMinAge() + " anos para a categoria " + category.getName());
            }
        }
    }
    
    @Transactional
    public void addPlayerToCategory(Long categoryId, Long playerId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com ID: " + categoryId));
                
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Jogador não encontrado com ID: " + playerId));

        // Valida se o player cumpre os requisitos da categoria
        validatePlayerAgeForCategory(player, category);

        // Evita duplicidade caso o player já esteja na categoria
        if (!category.getPlayers().contains(player)) {
            category.getPlayers().add(player);
            categoryRepository.save(category);
        }
    }
}