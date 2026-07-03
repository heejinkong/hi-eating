package kr.or.hieating.purchase.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.Data;

@Data
public class RecentPurchaseProductDto {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

  private Long purchaseId;
  private Long productId;
  private String productName;
  private String productStatus;
  private int quantity;
  private int purchasePrice;
  private LocalDateTime purchasedAt;
  private String pictureLocation;

  public int totalPrice() {
    return quantity * purchasePrice;
  }

  public String formattedTotalPrice() {
    return String.format(Locale.KOREA, "%,d원", totalPrice());
  }

  public String formattedQuantity() {
    return String.format(Locale.KOREA, "%,d개", quantity);
  }

  public String formattedPurchasedAt() {
    return purchasedAt.format(DATE_FORMAT);
  }

  public String statusLabel() {
    return switch (productStatus) {
      case "ON_SALE" -> "판매중";
      case "SOLD_OUT" -> "품절";
      case "STOPPED" -> "판매중지";
      default -> productStatus;
    };
  }
}
