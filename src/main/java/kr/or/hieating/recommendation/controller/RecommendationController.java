package kr.or.hieating.recommendation.controller;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.global.apiPayload.ApiResponse;
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
  public ApiResponse<RecommendationResponse> getRecommendations() {
    Long userId = userResolver.requireCurrentUserId();
    log.info("사용자 {} 추천 API 호출", userId);

    var products = recommendationService.recommend(userId);
    Set<Long> favoriteIds =
        favoriteService.findFavoriteProductIds(userId, products.stream().map(Product::id).toList());
    Map<Long, String> imageUrls =
        productImageService.getImageUrls(products.stream().map(Product::id).toList());

    var productDtos =
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

    return ApiResponse.onSuccess(new RecommendationResponse(productDtos));
  }

  @PostMapping("/initialize")
  public ApiResponse<String> initializeEmbeddings() {
    log.info("상품 임베딩 초기화 시작");

    productEmbeddingService.generateAllProductEmbeddings();
    return ApiResponse.onSuccess(
        "상품 임베딩 초기화 완료. 총 "
            + productEmbeddingService.getAllProductEmbeddings().size()
            + "개 상품 처리됨.");
  }

  @PostMapping("/refresh/{productId}")
  public ApiResponse<String> refreshProductEmbedding(@PathVariable Long productId) {
    log.info("상품 {} 임베딩 갱신 시작", productId);

    productEmbeddingService.updateProductEmbedding(productId);
    return ApiResponse.onSuccess("상품 " + productId + " 임베딩 갱신 완료.");
  }
}
