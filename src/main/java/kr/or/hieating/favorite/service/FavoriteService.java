package kr.or.hieating.favorite.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.or.hieating.favorite.dto.FavoriteProductListItemResponseDto;
import kr.or.hieating.favorite.dto.FavoriteProductListPageResponseDto;
import kr.or.hieating.favorite.dto.FavoriteProductSearchCondition;
import kr.or.hieating.favorite.mapper.FavoriteMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteMapper favoriteMapper;
  private final ImageUrlResolver imageUrlResolver;

  public FavoriteProductListPageResponseDto findFavoriteProducts(
      FavoriteProductSearchCondition condition) {
    int totalCount = favoriteMapper.countFavoriteProducts(condition);
    int totalPages = Math.max((int) Math.ceil((double) totalCount / condition.getSize()), 1);
    List<FavoriteProductListItemResponseDto> products =
        favoriteMapper.findFavoriteProducts(condition);

    products.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));

    return new FavoriteProductListPageResponseDto(
        products, condition.getPage(), condition.getSize(), totalCount, totalPages);
  }

  @Transactional
  public boolean toggleFavorite(Long userId, Long productId) {
    boolean alreadyFavorite = favoriteMapper.countFavorite(userId, productId) > 0;

    if (alreadyFavorite) {
      favoriteMapper.deleteFavorite(userId, productId);
      return false;
    }

    favoriteMapper.insertFavorite(userId, productId);
    return true;
  }

  public Set<Long> findFavoriteProductIds(Long userId, List<Long> productIds) {
    if (productIds == null || productIds.isEmpty()) {
      return Set.of();
    }

    return favoriteMapper.findFavoriteProductIds(userId, productIds).stream()
        .collect(Collectors.toSet());
  }
}
