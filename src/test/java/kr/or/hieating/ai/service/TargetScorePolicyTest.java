package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.or.hieating.ai.dto.HotDealTargetInfoDto;
import kr.or.hieating.ai.dto.UserProfileDto;
import kr.or.hieating.ai.dto.UserScoreDto;
import org.junit.jupiter.api.Test;

class TargetScorePolicyTest {

  private final TargetScorePolicy policy = new TargetScorePolicy();
  private final HotDealTargetInfoDto hotDeal =
      new HotDealTargetInfoDto(1L, "건강식품 할인", "설명", List.of("건강식품"), List.of());

  @Test
  void keepsHighActivityUserInHighScoreRange() {
    UserProfileDto profile = profile(1L, 5, 1, 2, true);

    UserScoreDto result =
        policy.normalize(hotDeal, profile, new UserScoreDto(1L, 50, "구매 가능성이 높습니다."));

    assertThat(result.score()).isEqualTo(90);
    assertThat(result.reason()).contains("구매 5회", "리뷰 1회", "즐겨찾기 2회");
  }

  @Test
  void limitsFavoriteOnlyUserToLowScoreRange() {
    UserProfileDto profile = profile(2L, 0, 0, 1, false);

    UserScoreDto result =
        policy.normalize(hotDeal, profile, new UserScoreDto(2L, 96, "관심도가 높습니다."));

    assertThat(result.score()).isEqualTo(59);
    assertThat(result.reason()).contains("구매 0회", "리뷰 0회", "즐겨찾기 1회");
  }

  private UserProfileDto profile(
      long userId, int purchases, int reviews, int favorites, boolean purchasedCategoryMatches) {
    return new UserProfileDto(
        userId,
        "OTHER",
        30,
        purchasedCategoryMatches ? List.of("건강식품") : List.of(),
        purchases,
        reviews > 0 ? List.of("건강식품") : List.of(),
        reviews,
        favorites > 0 ? List.of("건강식품") : List.of(),
        favorites,
        List.of("건강식품"),
        reviews > 0 ? 4.5 : null);
  }
}
