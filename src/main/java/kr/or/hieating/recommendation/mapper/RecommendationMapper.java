package kr.or.hieating.recommendation.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecommendationMapper {

  List<Long> findPurchasedProductIds(@Param("userId") Long userId);

  List<Long> findFavoriteProductIds(@Param("userId") Long userId);

  List<Long> findVisitedProductIds(@Param("userId") Long userId);

  List<Long> findHighRatedReviewProductIds(
      @Param("userId") Long userId, @Param("minRating") int minRating);
}
