package kr.or.hieating.product.dto;

import lombok.Data;

@Data
public class MostPurchasedProductResponseDto {

  private int productId;
  private String productName;
  private int price;
  private long totalQuantity;
  private String pictureLocation;
  private boolean favorite;
}
