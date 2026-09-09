package com.arenapointhub.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenapointhub.api.dto.CategoryRequestDTO;
import com.arenapointhub.api.dto.CategoryResponseDTO;
import com.arenapointhub.api.exception.BusinessException;
import com.arenapointhub.api.model.Category;
import com.arenapointhub.api.model.Tournament;
import com.arenapointhub.api.repository.CategoryRepository;
import com.arenapointhub.api.repository.TournamentRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;

    public CategoryService(CategoryRepository categoryRepository, TournamentRepository tournamentRepository) {
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
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

        if (category.getTournament() != null) {
            dto.setTournamentId(category.getTournament().getId());
        }

        return dto;
    }
}