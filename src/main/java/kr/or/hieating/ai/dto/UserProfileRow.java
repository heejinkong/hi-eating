package kr.or.hieating.ai.dto;

import java.time.LocalDate;

public record UserProfileRow(
    Long userId,
    String email,
    String gender,
    LocalDate birth,
    String purchaseCategories,
    Integer purchaseCount,
    String reviewCategories,
    Integer reviewCount,
    String favoriteCategories,
    Integer favoriteCount,
    Double averageRating) {}
