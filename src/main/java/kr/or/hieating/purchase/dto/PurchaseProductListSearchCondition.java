package kr.or.hieating.purchase.dto;

import lombok.Getter;

@Getter
public class PurchaseProductListSearchCondition {

  public static final int DEFAULT_SIZE = 10;

  private final Long userId;
  private final int page;
  private final int size;
  private final int offset;

  public PurchaseProductListSearchCondition(Long userId, Integer page) {
    this.userId = userId;
    this.page = Math.max(page == null ? 1 : page, 1);
    this.size = DEFAULT_SIZE;
    this.offset = (this.page - 1) * this.size;
  }
}
