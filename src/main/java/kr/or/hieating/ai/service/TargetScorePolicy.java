package kr.or.hieating.ai.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.or.hieating.ai.dto.HotDealTargetInfoDto;
import kr.or.hieating.ai.dto.UserProfileDto;
import kr.or.hieating.ai.dto.UserScoreDto;
import org.springframework.stereotype.Component;

@Component
public class TargetScorePolicy {

  public UserScoreDto normalize(
      HotDealTargetInfoDto hotDeal, UserProfileDto profile, UserScoreDto aiScore) {
    ScoreRange range = determineRange(hotDeal.categoryNames(), profile);
    int normalizedScore = Math.max(range.minimum(), Math.min(range.maximum(), aiScore.score()));
    String evidence =
        "구매 %d회, 리뷰 %d회, 즐겨찾기 %d회. AI 판단: %s"
            .formatted(
                profile.purchaseCount(),
                profile.reviewCount(),
                profile.favoriteCount(),
                aiScore.reason().trim());
    return new UserScoreDto(aiScore.userId(), normalizedScore, evidence);
  }

  private ScoreRange determineRange(List<String> hotDealCategories, UserProfileDto profile) {
    boolean purchasedCategoryMatches = intersects(hotDealCategories, profile.purchaseCategories());
    boolean reviewedCategoryMatches = intersects(hotDealCategories, profile.reviewCategories());
    boolean favoriteCategoryMatches = intersects(hotDealCategories, profile.favoriteCategories());

    if (purchasedCategoryMatches
        && profile.purchaseCount() >= 3
        && (reviewedCategoryMatches || favoriteCategoryMatches)) {
      return new ScoreRange(90, 100);
    }
    if (purchasedCategoryMatches
        && (profile.purchaseCount() >= 2 || (reviewedCategoryMatches && favoriteCategoryMatches))) {
      return new ScoreRange(80, 89);
    }
    if (purchasedCategoryMatches && profile.purchaseCount() >= 1) {
      return new ScoreRange(60, 79);
    }
    return new ScoreRange(0, 59);
  }

  private boolean intersects(List<String> left, List<String> right) {
    Set<String> values = new HashSet<>(left);
    return right.stream().anyMatch(values::contains);
  }

  private record ScoreRange(int minimum, int maximum) {}
}
