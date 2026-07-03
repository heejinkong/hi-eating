package kr.or.hieating.favorite.mapper;

import java.util.List;
import kr.or.hieating.favorite.dto.FavoriteProductListItemResponseDto;
import kr.or.hieating.favorite.dto.FavoriteProductSearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper {

  List<FavoriteProductListItemResponseDto> findFavoriteProducts(
      FavoriteProductSearchCondition condition);

  int countFavoriteProducts(FavoriteProductSearchCondition condition);

  int countFavoritesByUserId(@Param("userId") Long userId);

  int countFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

  int insertFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

  int deleteFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

  List<Long> findFavoriteProductIds(
      @Param("userId") Long userId, @Param("productIds") List<Long> productIds);
}
