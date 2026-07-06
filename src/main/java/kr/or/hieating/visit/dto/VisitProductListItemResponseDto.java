package kr.or.hieating.visit.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class VisitProductListItemResponseDto {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

  private Long productId;
  private String productName;
  private int price;
  private Integer salePrice;
  private int discountRate;
  private int viewCount;
  private int totalQuantity;
  private String pictureLocation;
  private LocalDateTime visitedAt;

  public String formattedVisitedAt() {
    return visitedAt.format(DATE_FORMAT);
  }
}
