package com.arenapointhub.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.GroupRequestDTO;
import com.arenapointhub.api.dto.GroupResponseDTO;
import com.arenapointhub.api.dto.PlayerSummaryDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Group;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.GroupRepository;
import com.arenapointhub.api.repository.PlayerRepository;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final CategoryRepository categoryRepository;
    private final PlayerRepository playerRepository;

    public GroupService(GroupRepository groupRepository, CategoryRepository categoryRepository, PlayerRepository playerRepository) {
        this.groupRepository = groupRepository;
        this.categoryRepository = categoryRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public GroupResponseDTO createGroup(GroupRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada com ID: " + dto.getCategoryId()));

        Group group = new Group();
        group.setName(dto.getName());
        group.setCategory(category);

        if (dto.getPlayerIds() != null && !dto.getPlayerIds().isEmpty()) {
            List<Player> players = playerRepository.findAllById(dto.getPlayerIds());
            if (players.size() != dto.getPlayerIds().size()) {
                throw new BusinessException("Um ou mais jogadores informados não foram encontrados.");
            }
            group.setPlayers(players);
        }

        Group savedGroup = groupRepository.save(group);
        return mapToResponseDTO(savedGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getGroupsByCategory(Long categoryId) {
        return groupRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado com ID: " + id));
        return mapToResponseDTO(group);
    }

    private GroupResponseDTO mapToResponseDTO(Group entity) {
        GroupResponseDTO dto = new GroupResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCategoryId(entity.getCategory().getId());
        dto.setCategoryName(entity.getCategory().getName());

        List<PlayerSummaryDTO> playerDTOs = entity.getPlayers() != null ?
                entity.getPlayers().stream()
                        .map(p -> new PlayerSummaryDTO(p.getId(), p.getName(), p.getClubAcademy()))
                        .collect(Collectors.toList()) : new ArrayList<>();

        dto.setPlayers(playerDTOs);
        return dto;
    }
}