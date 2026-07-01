package kr.or.hieating.review.mapper;

import java.util.List;
import kr.or.hieating.review.dto.ProductReviewResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {

  int countProductReviews(@Param("productId") Long productId);

  List<ProductReviewResponseDto> findProductReviews(
      @Param("productId") Long productId, @Param("offset") int offset, @Param("size") int size);
}
