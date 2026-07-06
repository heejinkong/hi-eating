package kr.or.hieating.recommendation.domain;

public record RecommendationResult(Long productId, Float similarityScore, Double weight) {}
