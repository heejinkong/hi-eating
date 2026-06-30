package kr.or.hieating.hotdeal.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HotDealProductsResponseDto {

  private int hotDealId;
  private int productOptionId;
  private String hotDealTitle;
  private String productName;
  private int originalPrice;
  private int hotDealPrice;
  private int discountRate;
  private int stock;
  private LocalDateTime endsAt;
  private long remainingSeconds;
}
