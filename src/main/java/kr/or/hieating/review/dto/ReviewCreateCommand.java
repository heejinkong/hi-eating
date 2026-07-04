package kr.or.hieating.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewCreateCommand {

  private Long id;
  private Long userId;
  private Long productId;
  private Long purchaseId;
  private int rating;
  private String content;
  private String imgSrc;
}
