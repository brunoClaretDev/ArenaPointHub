package com.arenapointhub.api.dto;

public class GroupGenerateRequestDTO {

    private Long categoryId;
    private int targetGroupSize = 3; // Tamanho padrão sugerido (ex: 3)

    public GroupGenerateRequestDTO() {
    }

    public GroupGenerateRequestDTO(Long categoryId, int targetGroupSize) {
        this.categoryId = categoryId;
        this.targetGroupSize = targetGroupSize;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getTargetGroupSize() {
        return targetGroupSize;
    }

    public void setTargetGroupSize(int targetGroupSize) {
        this.targetGroupSize = targetGroupSize;
    }
}