package kr.or.hieating.hotdeal.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HotDealProductListItemResponseDto {

  private Long hotDealId;
  private String hotDealTitle;
  private Long productId;
  private Long productOptionId;
  private String productName;
  private String pictureLocation;
  private int originalPrice;
  private int hotDealPrice;
  private int discountRate;
  private int stock;
  private int viewCount;
  private int totalQuantity;
  private LocalDateTime endsAt;
  private long remainingSeconds;
  private boolean favorite;
}
