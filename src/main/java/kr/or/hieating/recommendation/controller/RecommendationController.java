package kr.or.hieating.recommendation.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.product.domain.Product;
import kr.or.hieating.product.service.ProductImageService;
import kr.or.hieating.recommendation.dto.RecommendationResponse;
import kr.or.hieating.recommendation.dto.RecommendationResponse.ProductDto;
import kr.or.hieating.recommendation.service.ProductEmbeddingService;
import kr.or.hieating.recommendation.service.RecommendationService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

  private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

  private final ProductEmbeddingService productEmbeddingService;
  private final RecommendationService recommendationService;
  private final UserResolver userResolver;
  private final FavoriteService favoriteService;
  private final ProductImageService productImageService;

  @GetMapping("/products")
  public ResponseEntity<RecommendationResponse> getRecommendations() {
    Long userId = userResolver.requireCurrentUserId();
    log.info("사용자 {} 추천 API 호출", userId);

    try {
      List<Product> products = recommendationService.recommend(userId);
      Set<Long> favoriteIds =
          favoriteService.findFavoriteProductIds(
              userId, products.stream().map(Product::id).toList());
      Map<Long, String> imageUrls =
          productImageService.getImageUrls(products.stream().map(Product::id).toList());

      List<ProductDto> productDtos =
          products.stream()
              .map(
                  p ->
                      new ProductDto(
                          p.id(),
                          p.name(),
                          imageUrls.getOrDefault(p.id(), "/images/logo-hi-eating.png"),
                          favoriteIds.contains(p.id()),
                          p.formattedPrice()))
              .collect(Collectors.toList());

      return ResponseEntity.ok(new RecommendationResponse(productDtos));
    } catch (Exception e) {
      log.error("사용자 {} 추천 실패: {}", userId, e.getMessage(), e);
      return ResponseEntity.ok(new RecommendationResponse(List.of()));
    }
  }

  @PostMapping("/initialize")
  public ResponseEntity<String> initializeEmbeddings() {
    log.info("상품 임베딩 초기화 시작");

    try {
      productEmbeddingService.generateAllProductEmbeddings();
      return ResponseEntity.ok(
          "상품 임베딩 초기화 완료. 총 "
              + productEmbeddingService.getAllProductEmbeddings().size()
              + "개 상품 처리됨.");
    } catch (Exception e) {
      log.error("상품 임베딩 초기화 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().body("임베딩 초기화 실패: " + e.getMessage());
    }
  }

  @PostMapping("/refresh/{productId}")
  public ResponseEntity<String> refreshProductEmbedding(@PathVariable Long productId) {
    log.info("상품 {} 임베딩 갱신 시작", productId);

    try {
      productEmbeddingService.updateProductEmbedding(productId);
      return ResponseEntity.ok("상품 " + productId + " 임베딩 갱신 완료.");
    } catch (Exception e) {
      log.error("상품 {} 임베딩 갱신 실패: {}", productId, e.getMessage(), e);
      return ResponseEntity.internalServerError().body("임베딩 갱신 실패: " + e.getMessage());
    }
  }
}
