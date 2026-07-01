package kr.or.hieating.product.domain;

import java.time.LocalDateTime;
import java.util.Locale;

public record Product(
    Long id,
    Long categoryId,
    String name,
    String description,
    int price,
    int viewCount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public String formattedPrice() {
    return String.format(Locale.KOREA, "%,d원", price);
  }

  public String statusLabel() {
    return switch (status) {
      case "ON_SALE" -> "판매중";
      case "SOLD_OUT" -> "품절";
      case "STOPPED" -> "판매중지";
      default -> status;
    };
  }
}
