package kr.or.hieating.review.mapper;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.review.dto.ProductReviewResponseDto;
import kr.or.hieating.review.dto.ReviewCreateCommand;
import kr.or.hieating.review.dto.ReviewFormResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {

  int countProductReviews(@Param("productId") Long productId);

  List<ProductReviewResponseDto> findProductReviews(
      @Param("productId") Long productId, @Param("offset") int offset, @Param("size") int size);

  Optional<ReviewFormResponseDto> findReviewFormByPurchaseId(
      @Param("userId") Long userId, @Param("purchaseId") Long purchaseId);

  Optional<ReviewFormResponseDto> findLatestReviewFormByProductId(
      @Param("userId") Long userId, @Param("productId") Long productId);

  int countReviewByPurchaseId(@Param("purchaseId") Long purchaseId);

  int insertReview(ReviewCreateCommand command);
}
