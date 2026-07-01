package kr.or.hieating.visit.domain;

import java.time.LocalDateTime;

public record Visit(
    Long userId, Long productId, LocalDateTime createdAt, LocalDateTime updatedAt) {}
