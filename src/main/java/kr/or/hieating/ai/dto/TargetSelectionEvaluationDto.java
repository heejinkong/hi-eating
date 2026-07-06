package kr.or.hieating.ai.dto;

public record TargetSelectionEvaluationDto(
    Long userId, Integer score, String reason, String decision) {}
