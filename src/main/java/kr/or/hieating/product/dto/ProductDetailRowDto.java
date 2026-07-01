package kr.or.hieating.product.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductDetailRowDto {

  private Long id;
  private Long categoryId;
  private String categoryName;
  private String name;
  private String description;
  private int price;
  private int viewCount;
  private String status;
  private LocalDateTime createdAt;
  private double averageRating;
  private int reviewCount;
}
