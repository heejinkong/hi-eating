package kr.or.hieating.review.service;

import java.util.List;
import java.util.Objects;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.review.config.ReviewImageUploadClient;
import kr.or.hieating.review.domain.Reviews;
import kr.or.hieating.review.dto.ProductReviewPageResponseDto;
import kr.or.hieating.review.dto.ProductReviewResponseDto;
import kr.or.hieating.review.dto.ReviewCreateCommand;
import kr.or.hieating.review.dto.ReviewCreateRequestDto;
import kr.or.hieating.review.dto.ReviewCreateResponseDto;
import kr.or.hieating.review.dto.ReviewFormResponseDto;
import kr.or.hieating.review.mapper.ReviewMapper;
import kr.or.hieating.review.utils.ReviewImageUrlResolver;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

  private static final String FALLBACK_PRODUCT_IMAGE_URL = "/images/logo-hi-eating.png";

  private final ReviewMapper reviewMapper;
  private final ImageUrlResolver imageUrlResolver;
  private final ReviewImageUrlResolver reviewImageUrlResolver;
  private final ReviewImageUploadClient reviewImageUploadClient;
  private final TransactionTemplate transactionTemplate;

  public ReviewFormResponseDto findReviewForm(Long userId, Long purchaseId, Long productId) {
    ReviewFormResponseDto reviewForm =
        purchaseId != null
            ? reviewMapper
                .findReviewFormByPurchaseId(userId, purchaseId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REVIEW_FORM_NOT_FOUND))
            : reviewMapper
                .findLatestReviewFormByProductId(userId, productId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REVIEW_FORM_NOT_FOUND));

    String productImageUrl = imageUrlResolver.resolve(reviewForm.getProductImageUrl());
    reviewForm.setProductImageUrl(
        productImageUrl == null || productImageUrl.isBlank()
            ? FALLBACK_PRODUCT_IMAGE_URL
            : productImageUrl);
    reviewForm.setReview(createEmptyReview(reviewForm));
    return reviewForm;
  }

  public ProductReviewPageResponseDto findProductReviews(Long productId, int page, int size) {
    int totalCount = reviewMapper.countProductReviews(productId);
    int totalPages = (int) Math.ceil((double) totalCount / size);
    int normalizedPage = normalizePage(page, totalPages);
    int offset = (normalizedPage - 1) * size;

    List<ProductReviewResponseDto> reviews =
        totalCount == 0 ? List.of() : reviewMapper.findProductReviews(productId, offset, size);
    reviews.forEach(review -> review.setImgSrc(reviewImageUrlResolver.resolve(review.getImgSrc())));

    return new ProductReviewPageResponseDto(reviews, normalizedPage, size, totalCount, totalPages);
  }

  public ReviewCreateResponseDto createReview(Long userId, ReviewCreateRequestDto request) {
    if (reviewMapper.countReviewByPurchaseId(request.getPurchaseId()) > 0) {
      throw new GeneralException(ErrorStatus.DUPLICATE_REVIEW);
    }

    ReviewFormResponseDto reviewForm =
        reviewMapper
            .findReviewFormByPurchaseId(userId, request.getPurchaseId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.REVIEW_FORM_NOT_FOUND));

    if (!Objects.equals(reviewForm.getProductId(), request.getProductId())) {
      throw new GeneralException(ErrorStatus.INVALID_REVIEW_TARGET);
    }

    String imgSrc = uploadReviewImageIfExists(request.getReviewImage());
    try {
      return transactionTemplate.execute(status -> insertReview(userId, request, imgSrc));
    } catch (RuntimeException e) {
      log.warn("Review insert failed after image upload. orphanImgSrc={}", imgSrc, e);
      throw e;
    }
  }

  private ReviewCreateResponseDto insertReview(
      Long userId, ReviewCreateRequestDto request, String imgSrc) {
    ReviewCreateCommand command =
        new ReviewCreateCommand(
            null,
            userId,
            request.getProductId(),
            request.getPurchaseId(),
            request.getRating(),
            request.getContent(),
            imgSrc);

    reviewMapper.insertReview(command);
    return new ReviewCreateResponseDto(
        command.getId(),
        request.getProductId(),
        request.getPurchaseId(),
        reviewImageUrlResolver.resolve(imgSrc),
        "/product/" + request.getProductId());
  }

  private String uploadReviewImageIfExists(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    return reviewImageUploadClient.upload(file);
  }

  private Reviews createEmptyReview(ReviewFormResponseDto reviewForm) {
    Reviews review = new Reviews();
    review.setProductId(reviewForm.getProductId());
    review.setPurchaseId(reviewForm.getPurchaseId());
    review.setRating(0);
    review.setContent("");
    review.setImgSrc("");
    return review;
  }

  private int normalizePage(int page, int totalPages) {
    int normalizedPage = Math.max(page, 1);

    if (totalPages > 0) {
      return Math.min(normalizedPage, totalPages);
    }

    return 1;
  }
}
