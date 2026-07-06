package kr.or.hieating.visit.dto;

import lombok.Getter;

@Getter
public class VisitProductListSearchCondition {

  public static final int DEFAULT_SIZE = 12;

  private final Long userId;
  private final int page;
  private final int size;
  private final int offset;

  public VisitProductListSearchCondition(Long userId, Integer page) {
    this.userId = userId;
    this.page = Math.max(page == null ? 1 : page, 1);
    this.size = DEFAULT_SIZE;
    this.offset = (this.page - 1) * this.size;
  }
}
