package kr.or.hieating.product.admin.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponseDTO {
  private Long productId;
  private Long productOptionId;
  private String name;
  private String categoryName;
  private Integer price;
  private Integer stock;
  private LocalDate expireDate;

  public String getStatus() {
    if (this.expireDate != null && this.expireDate.isBefore(LocalDate.now())) {
      return "폐기";
    }

    if (this.expireDate == null) {
      return "일반";
    }

    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), this.expireDate);

    return (daysLeft >= 0 && daysLeft <= 7) ? "유통임박" : "일반";
  }
}
