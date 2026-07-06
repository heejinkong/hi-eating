package kr.or.hieating.ai.dto;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record UserProfileDto(
    Long userId,
    String gender,
    Integer age,
    List<String> purchaseCategories,
    Integer purchaseCount,
    List<String> reviewCategories,
    Integer reviewCount,
    List<String> favoriteCategories,
    Integer favoriteCount,
    List<String> interestCategories,
    Double averageRating) {

  public static UserProfileDto from(UserProfileRow row) {
    List<String> purchaseCategories = splitCsv(row.purchaseCategories());
    List<String> reviewCategories = splitCsv(row.reviewCategories());
    List<String> favoriteCategories = splitCsv(row.favoriteCategories());
    Set<String> interests = new LinkedHashSet<>();
    interests.addAll(purchaseCategories);
    interests.addAll(reviewCategories);
    interests.addAll(favoriteCategories);

    return new UserProfileDto(
        row.userId(),
        row.gender(),
        Period.between(row.birth(), LocalDate.now()).getYears(),
        purchaseCategories,
        Objects.requireNonNullElse(row.purchaseCount(), 0),
        reviewCategories,
        Objects.requireNonNullElse(row.reviewCount(), 0),
        favoriteCategories,
        Objects.requireNonNullElse(row.favoriteCount(), 0),
        List.copyOf(interests),
        row.averageRating());
  }

  static List<String> splitCsv(String csv) {
    if (csv == null || csv.isBlank()) {
      return List.of();
    }
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .distinct()
        .toList();
  }
}
