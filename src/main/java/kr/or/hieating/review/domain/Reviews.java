package kr.or.hieating.review.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Reviews {

  private int id;
  private int userId;
  private int productId;
  private int purchaseId;
  private int rating;
  private String content;
  private String imgSrc;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
