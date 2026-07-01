package kr.or.hieating.hotdeal.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotDealProducts {

  private int hotDealId;
  private int productOptionId;
  private int originalPrice;
  private int hotDealPrice;
  private LocalDateTime createdAt;
}
