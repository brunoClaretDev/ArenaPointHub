package com.arenapointhub.api.dto;

import java.util.List;

public class BracketResponseDTO {

    private Long categoryId;
    private String categoryName;
    private int targetBracketSize;
    private List<MatchResponseDTO> matches;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getTargetBracketSize() {
        return targetBracketSize;
    }

    public void setTargetBracketSize(int targetBracketSize) {
        this.targetBracketSize = targetBracketSize;
    }

    public List<MatchResponseDTO> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchResponseDTO> matches) {
        this.matches = matches;
    }
}