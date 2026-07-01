package kr.or.hieating.review.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ProductReview(
    Long id,
    Long userId,
    Long productId,
    Long purchaseId,
    String reviewerName,
    int rating,
    String content,
    String imgSrc,
    LocalDateTime createdAt,
    String purchasedOption) {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

  public ProductReview {
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("rating must be between 1 and 5");
    }
  }

  public String starText() {
    return "★".repeat(rating) + "☆".repeat(5 - rating);
  }

  public String formattedCreatedAt() {
    return createdAt.format(DATE_FORMAT);
  }
}
