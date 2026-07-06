package kr.or.hieating.ai.dto;

public record TargetSelectionJobDto(
    Long id, Long hotDealId, String status, Integer retryCount, Integer maxRetryCount) {}
