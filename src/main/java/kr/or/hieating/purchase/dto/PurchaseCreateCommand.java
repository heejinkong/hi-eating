package kr.or.hieating.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PurchaseCreateCommand {

  private Long id;
  private Long userId;
  private Long productId;
  private int quantity;
  private int purchasePrice;
}
