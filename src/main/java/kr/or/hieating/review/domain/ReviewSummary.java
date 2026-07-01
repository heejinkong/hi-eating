package kr.or.hieating.review.domain;

import java.util.Locale;

public record ReviewSummary(double averageRating, int reviewCount) {

  public String averageLabel() {
    return String.format(Locale.KOREA, "%.1f", averageRating);
  }

  public String formattedReviewCount() {
    return String.format(Locale.KOREA, "%,d", reviewCount);
  }
}
