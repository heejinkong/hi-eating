package kr.or.hieating.recommendation.domain;

import java.util.List;

public record ProductEmbedding(Long productId, List<Float> embedding, Integer embeddingDimension) {}
