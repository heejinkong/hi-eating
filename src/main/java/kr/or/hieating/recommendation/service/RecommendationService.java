package kr.or.hieating.recommendation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.or.hieating.product.domain.Product;
import kr.or.hieating.product.mapper.ProductMapper;
import kr.or.hieating.recommendation.domain.ProductEmbedding;
import kr.or.hieating.recommendation.domain.RecommendationResult;
import kr.or.hieating.recommendation.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {

  private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

  private final UserProfileService userProfileService;
  private final ProductEmbeddingService productEmbeddingService;
  private final ProductMapper productMapper;

  private static final int TOP_N = 7;
  private static final double MIN_SIMILARITY_THRESHOLD = 0.0;

  public List<Product> recommend(Long userId) {
    log.info("사용자 {} 추천 시작", userId);

    try {
      UserProfile userProfile = userProfileService.buildUserProfile(userId);
      log.debug("사용자 프로필 차원: {}", userProfile.embeddingDimension());

      List<Product> allProducts = productMapper.findAllActiveProducts();
      Map<Long, Product> productMap = new HashMap<>();
      for (Product p : allProducts) {
        productMap.put(p.id(), p);
      }

      log.debug("전체 상품 수: {}", allProducts.size());

      List<RecommendationResult> results = new ArrayList<>();

      for (Product product : allProducts) {
        ProductEmbedding embedding;
        try {
          embedding = productEmbeddingService.getProductEmbedding(product.id());
        } catch (Exception e) {
          log.debug("상품 {}의 임베딩이 없음", product.name());
          continue;
        }

        float similarity = cosineSimilarity(userProfile.embedding(), embedding.embedding());

        if (similarity >= MIN_SIMILARITY_THRESHOLD) {
          results.add(new RecommendationResult(product.id(), similarity, 0.0));
        }
      }

      results.sort(Comparator.comparing(RecommendationResult::similarityScore).reversed());

      List<Product> recommendedProducts =
          results.stream()
              .limit(TOP_N)
              .map(RecommendationResult::productId)
              .map(productMap::get)
              .collect(Collectors.toList());

      log.info("사용자 {} 추천 완료. 추천 상품 수: {}", userId, recommendedProducts.size());

      return recommendedProducts;

    } catch (Exception e) {
      log.error("사용자 {} 추천 실패: {}", userId, e.getMessage(), e);
      throw new RuntimeException("추천 실패: " + e.getMessage(), e);
    }
  }

  private float cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
    if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
      return 0.0f;
    }

    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;

    int size = Math.min(vectorA.size(), vectorB.size());
    for (int i = 0; i < size; i++) {
      double a = vectorA.get(i);
      double b = vectorB.get(i);
      dotProduct += a * b;
      normA += a * a;
      normB += b * b;
    }

    if (normA == 0.0 || normB == 0.0) {
      return 0.0f;
    }

    return (float) (dotProduct / Math.sqrt(normA * normB));
  }
}
