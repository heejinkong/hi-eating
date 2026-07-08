package kr.or.hieating.review.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Reviews {

  private Long id;
  private Long userId;
  private Long productId;
  private Long purchaseId;
  private int rating;
  private String content;
  private String imgSrc;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
