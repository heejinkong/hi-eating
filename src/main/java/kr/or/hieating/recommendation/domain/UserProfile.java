package kr.or.hieating.recommendation.domain;

import java.util.List;

public record UserProfile(Long userId, List<Float> embedding, Integer embeddingDimension) {}
