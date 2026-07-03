package kr.or.hieating.purchase.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record Purchase(
    Long id,
    Long userId,
    Long productId,
    int quantity,
    int purchasePrice,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

  public int totalPrice() {
    return quantity * purchasePrice;
  }

  public String formattedTotalPrice() {
    return String.format(Locale.KOREA, "%,d원", totalPrice());
  }

  public String formattedQuantity() {
    return String.format(Locale.KOREA, "%,d개", quantity);
  }

  public String formattedCreatedAt() {
    return createdAt.format(DATE_FORMAT);
  }
}
