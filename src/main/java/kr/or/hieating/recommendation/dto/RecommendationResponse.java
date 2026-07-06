package kr.or.hieating.recommendation.dto;

import java.util.List;

public record RecommendationResponse(List<ProductDto> products) {
  public record ProductDto(
      Long id, String name, String imageUrl, boolean favorited, String formattedPrice) {}
}
