package kr.or.hieating.hotdeal.admin.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotDealDetailResponseDTO {
  private int id;
  private String title;
  private String description;
  private LocalDateTime startsAt;
  private LocalDateTime endsAt;
  private Integer discountRate;
  private List<ProductItemDTO> products;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductItemDTO {
    private Integer productOptionId;
    private String productName;
    private Integer originalPrice;
    private Integer hotDealPrice;
  }
}
