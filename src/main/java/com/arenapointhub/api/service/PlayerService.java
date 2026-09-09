package com.arenapointhub.api.service;

import com.arenapointhub.api.dto.PlayerRequestDTO;
import com.arenapointhub.api.dto.PlayerResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Player;
import com.arenapointhub.api.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerResponseDTO createPlayer(PlayerRequestDTO dto) {
        if (playerRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Já existe um jogador cadastrado com o e-mail: " + dto.getEmail());
        }

        Player player = new Player();
        copyDtoToEntity(dto, player);

        Player savedPlayer = playerRepository.save(player);
        return mapToResponseDTO(savedPlayer);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponseDTO> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlayerResponseDTO getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Jogador não encontrado com o ID: " + id));
        return mapToResponseDTO(player);
    }

    @Transactional
    public PlayerResponseDTO updatePlayer(Long id, PlayerRequestDTO dto) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Jogador não encontrado com o ID: " + id));

        // Valida se o e-mail mudou e se o novo e-mail já pertence a outro jogador
        if (!player.getEmail().equalsIgnoreCase(dto.getEmail()) && playerRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Já existe outro jogador cadastrado com o e-mail: " + dto.getEmail());
        }

        copyDtoToEntity(dto, player);
        Player updatedPlayer = playerRepository.save(player);
        return mapToResponseDTO(updatedPlayer);
    }

    @Transactional
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new BusinessException("Jogador não encontrado com o ID: " + id);
        }
        playerRepository.deleteById(id);
    }

    private void copyDtoToEntity(PlayerRequestDTO dto, Player entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setBirthDate(dto.getBirthDate());
        entity.setPhone(dto.getPhone());
        entity.setClubAcademy(dto.getClubAcademy());
    }

    private PlayerResponseDTO mapToResponseDTO(Player player) {
        PlayerResponseDTO dto = new PlayerResponseDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setEmail(player.getEmail());
        dto.setBirthDate(player.getBirthDate());
        dto.setPhone(player.getPhone());
        dto.setClubAcademy(player.getClubAcademy());
        return dto;
    }
}