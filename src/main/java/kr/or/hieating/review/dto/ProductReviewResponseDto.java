package kr.or.hieating.review.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductReviewResponseDto {

  private Long id;
  private Long userId;
  private Long productId;
  private Long purchaseId;
  private String reviewerName;
  private int rating;
  private String content;
  private String imgSrc;
  private LocalDateTime createdAt;
}
