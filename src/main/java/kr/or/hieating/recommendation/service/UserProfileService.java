package kr.or.hieating.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kr.or.hieating.recommendation.domain.UserProfile;
import kr.or.hieating.recommendation.mapper.RecommendationMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

  private final RecommendationMapper recommendationMapper;
  private final ProductEmbeddingService productEmbeddingService;

  public UserProfile buildUserProfile(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("사용자 ID가 필요합니다.");
    }

    log.info("사용자 {} 프로필 벡터 생성 시작", userId);

    List<UserActionItem> actionItems = collectUserActions(userId);

    if (actionItems.isEmpty()) {
      log.warn("사용자 {}의 행동 데이터가 없습니다.", userId);
      return new UserProfile(userId, new ArrayList<>(), 0);
    }

    List<String> textsToEmbed =
        actionItems.stream().map(item -> item.text).collect(Collectors.toList());

    List<List<Float>> actionEmbeddings =
        productEmbeddingService.getEmbeddingsForMultipleTexts(textsToEmbed);

    int dimension = 0;
    if (!actionEmbeddings.isEmpty() && !actionEmbeddings.get(0).isEmpty()) {
      dimension = actionEmbeddings.get(0).size();
    }

    if (dimension == 0) {
      log.warn("임베딩 차원이 0입니다.");
      return new UserProfile(userId, new ArrayList<>(), 0);
    }

    double totalWeight = 0;
    double[] weightedSum = new double[dimension];

    for (int i = 0; i < actionItems.size(); i++) {
      double weight = actionItems.get(i).weight;
      List<Float> embedding = actionEmbeddings.get(i);

      if (embedding == null || embedding.isEmpty()) {
        continue;
      }

      totalWeight += weight;
      for (int j = 0; j < dimension && j < embedding.size(); j++) {
        weightedSum[j] += embedding.get(j) * weight;
      }
    }

    if (totalWeight == 0) {
      log.warn("사용자 {}의 유효한 임베딩이 없습니다.", userId);
      return new UserProfile(userId, new ArrayList<>(), 0);
    }

    List<Float> normalized = new ArrayList<>(dimension);
    for (int j = 0; j < dimension; j++) {
      normalized.add((float) (weightedSum[j] / totalWeight));
    }

    log.info("사용자 {} 프로필 벡터 생성 완료. 차원: {}, 항목 수: {}", userId, dimension, actionItems.size());

    return new UserProfile(userId, normalized, dimension);
  }

  private List<UserActionItem> collectUserActions(Long userId) {
    List<Long> purchasedIds = recommendationMapper.findPurchasedProductIds(userId);
    List<Long> favoriteIds = recommendationMapper.findFavoriteProductIds(userId);
    List<Long> visitedIds = recommendationMapper.findVisitedProductIds(userId);
    List<Long> highRatedIds = recommendationMapper.findHighRatedReviewProductIds(userId, 4);

    log.debug(
        "구매: {}, 찜: {}, 방문: {}, 높은평점리뷰: {}",
        purchasedIds.size(),
        favoriteIds.size(),
        visitedIds.size(),
        highRatedIds.size());

    List<UserActionItem> items = new ArrayList<>();

    for (Long id : purchasedIds) {
      try {
        String text = productEmbeddingService.getProductName(id);
        items.add(new UserActionItem(text, 3.0));
      } catch (Exception e) {
        log.debug("구매 상품 {} 텍스트 조회 실패", id);
      }
    }

    for (Long id : favoriteIds) {
      if (purchasedIds.contains(id)) continue;
      try {
        String text = productEmbeddingService.getProductName(id);
        items.add(new UserActionItem(text, 2.0));
      } catch (Exception e) {
        log.debug("찜 상품 {} 텍스트 조회 실패", id);
      }
    }

    for (Long id : visitedIds) {
      if (purchasedIds.contains(id) || favoriteIds.contains(id)) continue;
      try {
        String text = productEmbeddingService.getProductName(id);
        items.add(new UserActionItem(text, 1.0));
      } catch (Exception e) {
        log.debug("방문 상품 {} 텍스트 조회 실패", id);
      }
    }

    for (Long id : highRatedIds) {
      if (purchasedIds.contains(id) || favoriteIds.contains(id) || visitedIds.contains(id))
        continue;
      try {
        String text = productEmbeddingService.getProductName(id);
        items.add(new UserActionItem(text, 1.5));
      } catch (Exception e) {
        log.debug("리뷰 상품 {} 텍스트 조회 실패", id);
      }
    }

    return items;
  }

  private record UserActionItem(String text, double weight) {}
}
