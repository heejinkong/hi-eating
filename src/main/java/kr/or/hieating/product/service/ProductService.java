package kr.or.hieating.product.service;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.product.domain.ProductDetail;
import kr.or.hieating.product.domain.ProductOption;
import kr.or.hieating.product.dto.MostPurchasedProductResponseDto;
import kr.or.hieating.product.dto.ProductDetailRowDto;
import kr.or.hieating.product.dto.ProductListItemResponseDto;
import kr.or.hieating.product.dto.ProductListPageResponseDto;
import kr.or.hieating.product.dto.ProductListSearchCondition;
import kr.or.hieating.product.mapper.ProductMapper;
import kr.or.hieating.review.domain.ReviewSummary;
import kr.or.hieating.utils.ImageUrlResolver;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private static final List<String> FALLBACK_IMAGE_URLS = List.of("/images/logo-hi-eating.png");

  private final ProductMapper productMapper;
  private final ImageUrlResolver imageUrlResolver;
  private final UserResolver userResolver;
  private final FavoriteService favoriteService;

  public List<MostPurchasedProductResponseDto> findMostPurchasedProducts() {
    List<MostPurchasedProductResponseDto> products =
        productMapper.findMostPurchasedProducts(userResolver.currentUserIdOrNull());
    products.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));
    return products;
  }

  public Optional<ProductDetail> findProductDetail(Long productId) {
    return productMapper
        .findProductDetailRow(productId)
        .map(product -> createProductDetail(product, productId));
  }

  private ProductDetail createProductDetail(ProductDetailRowDto product, Long productId) {
    Long userId = userResolver.currentUserIdOrNull();
    List<String> imageUrls =
        productMapper.findProductImageUrls(productId).stream()
            .map(imageUrlResolver::resolve)
            .toList();
    List<ProductOption> options = productMapper.findProductOptions(productId);
    boolean favorite =
        favoriteService.findFavoriteProductIds(userId, List.of(productId)).contains(productId);

    if (imageUrls.isEmpty()) {
      imageUrls = FALLBACK_IMAGE_URLS;
    }

    return new ProductDetail(
        product.getId(),
        product.getCategoryId(),
        product.getCategoryName(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getSalePrice(),
        product.getDiscountRate(),
        product.getViewCount(),
        product.getStatus(),
        product.getCreatedAt(),
        imageUrls,
        options,
        new ReviewSummary(product.getAverageRating(), product.getReviewCount()),
        favorite);
  }

  public ProductListPageResponseDto findProductsByCategory(ProductListSearchCondition condition) {
    int totalCount = productMapper.countProductsByCategory(condition);
    int totalPages = Math.max((int) Math.ceil((double) totalCount / condition.getSize()), 1);
    List<ProductListItemResponseDto> products = productMapper.findProductsByCategory(condition);
    products.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));

    return new ProductListPageResponseDto(
        products, condition.getPage(), condition.getSize(), totalCount, totalPages);
  }
}
