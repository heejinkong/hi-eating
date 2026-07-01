package kr.or.hieating.review.service;

import java.util.List;
import kr.or.hieating.review.dto.ProductReviewPageResponseDto;
import kr.or.hieating.review.dto.ProductReviewResponseDto;
import kr.or.hieating.review.mapper.ReviewMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewMapper reviewMapper;
  private final ImageUrlResolver imageUrlResolver;

  public ProductReviewPageResponseDto findProductReviews(Long productId, int page, int size) {
    int totalCount = reviewMapper.countProductReviews(productId);
    int totalPages = (int) Math.ceil((double) totalCount / size);
    int normalizedPage = normalizePage(page, totalPages);
    int offset = (normalizedPage - 1) * size;

    List<ProductReviewResponseDto> reviews =
        totalCount == 0 ? List.of() : reviewMapper.findProductReviews(productId, offset, size);
    reviews.forEach(review -> review.setImgSrc(imageUrlResolver.resolve(review.getImgSrc())));

    return new ProductReviewPageResponseDto(reviews, normalizedPage, size, totalCount, totalPages);
  }

  private int normalizePage(int page, int totalPages) {
    int normalizedPage = Math.max(page, 1);

    if (totalPages > 0) {
      return Math.min(normalizedPage, totalPages);
    }

    return 1;
  }
}
