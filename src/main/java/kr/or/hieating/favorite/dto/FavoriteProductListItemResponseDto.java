package kr.or.hieating.favorite.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FavoriteProductListItemResponseDto {

  private Long productId;
  private String productName;
  private int price;
  private Integer salePrice;
  private int discountRate;
  private int viewCount;
  private int totalQuantity;
  private String pictureLocation;
  private LocalDateTime favoriteCreatedAt;
}
