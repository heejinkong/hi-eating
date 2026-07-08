package kr.or.hieating.hotdeal.dto;

import lombok.Getter;

@Getter
public class HotDealProductSearchCondition {

  public static final int DEFAULT_SIZE = 20;
  private static final int MAX_SIZE = 40;

  private final Long hotDealId;
  private final Long userId;
  private final String sort;
  private final int page;
  private final int size;
  private final int offset;
  private final int limit;

  public HotDealProductSearchCondition(Long hotDealId, String sort, Integer page, Integer size) {
    this(hotDealId, null, sort, page, size);
  }

  public HotDealProductSearchCondition(
      Long hotDealId, Long userId, String sort, Integer page, Integer size) {
    this.hotDealId = hotDealId;
    this.userId = userId;
    this.sort = normalizeSort(sort);
    this.page = Math.max(page == null ? 1 : page, 1);
    this.size = normalizeSize(size);
    this.offset = (this.page - 1) * this.size;
    this.limit = this.size;
  }

  private static int normalizeSize(Integer size) {
    if (size == null) {
      return DEFAULT_SIZE;
    }
    return Math.min(Math.max(size, 1), MAX_SIZE);
  }

  private static String normalizeSort(String sort) {
    return switch (sort == null ? "popular" : sort) {
      case "latest", "priceAsc", "priceDesc" -> sort;
      default -> "popular";
    };
  }
}
