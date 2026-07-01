package kr.or.hieating.review.dto;

import java.util.List;

public record ProductReviewPageResponseDto(
    List<ProductReviewResponseDto> items, int page, int size, int totalCount, int totalPages) {}
