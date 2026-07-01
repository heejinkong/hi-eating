package kr.or.hieating.product.dto;

import lombok.Data;

@Data
public class ProductListItemResponseDto {

  private Long productId;
  private String productName;
  private int price;
  private Integer salePrice;
  private int discountRate;
  private int viewCount;
  private int totalQuantity;
  private String pictureLocation;
}
