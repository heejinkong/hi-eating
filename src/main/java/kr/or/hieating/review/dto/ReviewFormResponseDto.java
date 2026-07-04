package kr.or.hieating.review.dto;

import kr.or.hieating.review.domain.Reviews;
import lombok.Data;

@Data
public class ReviewFormResponseDto {

  private Long purchaseId;
  private Long productId;
  private String brandName;
  private String productName;
  private String optionName;
  private String productImageUrl;
  private Reviews review;
}
