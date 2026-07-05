package kr.or.hieating.tabledecor.dto;

import lombok.Getter;

@Getter
public class TableDecorPostSearchCondition {

  public static final int DEFAULT_SIZE = 3;

  private final int page;
  private final int size;
  private final int offset;
  private final Long currentUserId;

  public TableDecorPostSearchCondition(Integer page, Long currentUserId) {
    this.page = Math.max(page == null ? 1 : page, 1);
    this.size = DEFAULT_SIZE;
    this.offset = (this.page - 1) * this.size;
    this.currentUserId = currentUserId;
  }
}
