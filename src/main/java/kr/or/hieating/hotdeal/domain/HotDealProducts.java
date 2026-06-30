package kr.or.hieating.hotdeal.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HotDealProducts {

  private int hotDealId;
  private int productOptionId;
  private int originalPrice;
  private int hotDealPrice;
  private LocalDateTime createdAt;
}
