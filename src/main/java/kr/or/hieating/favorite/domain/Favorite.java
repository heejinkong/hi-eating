package kr.or.hieating.favorite.domain;

import java.time.LocalDateTime;

public record Favorite(
    Long userId, Long productId, LocalDateTime createdAt, LocalDateTime updatedAt) {}
