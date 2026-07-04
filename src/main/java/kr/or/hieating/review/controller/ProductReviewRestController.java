package kr.or.hieating.review.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.review.dto.ProductReviewPageResponseDto;
import kr.or.hieating.review.dto.ReviewCreateRequestDto;
import kr.or.hieating.review.dto.ReviewCreateResponseDto;
import kr.or.hieating.review.service.ReviewService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Validated
public class ProductReviewRestController {

  private static final int MIN_PAGE_SIZE = 1;
  private static final int MAX_PAGE_SIZE = 10;

  private final ReviewService reviewService;
  private final UserResolver userResolver;

  @GetMapping("/api/products/{productId}/reviews")
  public ApiResponse<ProductReviewPageResponseDto> productReviews(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "5") int size) {
    ProductReviewPageResponseDto result =
        reviewService.findProductReviews(productId, page, normalizeSize(size));
    return ApiResponse.onSuccess(result);
  }

  @PostMapping(value = "/api/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<ReviewCreateResponseDto> createReview(
      @RequestParam @NotNull(message = "구매 정보가 필요합니다.") Long purchaseId,
      @RequestParam @NotNull(message = "상품 정보가 필요합니다.") Long productId,
      @RequestParam
          @Min(value = 1, message = "별점은 1점 이상이어야 합니다.") @Max(value = 5, message = "별점은 5점 이하여야 합니다.") int rating,
      @RequestParam
          @NotBlank(message = "리뷰 내용을 입력해주세요.") @Size(max = 500, message = "리뷰 내용은 500자 이하여야 합니다.") String content,
      @RequestParam(value = "reviewImage", required = false) MultipartFile reviewImage) {
    ReviewCreateRequestDto request = new ReviewCreateRequestDto();
    request.setPurchaseId(purchaseId);
    request.setProductId(productId);
    request.setRating(rating);
    request.setContent(content);
    request.setReviewImage(reviewImage);

    ReviewCreateResponseDto result =
        reviewService.createReview(userResolver.requireCurrentUserId(), request);
    return ApiResponse.onSuccess(result);
  }

  private int normalizeSize(int size) {
    return Math.max(MIN_PAGE_SIZE, Math.min(size, MAX_PAGE_SIZE));
  }
}
