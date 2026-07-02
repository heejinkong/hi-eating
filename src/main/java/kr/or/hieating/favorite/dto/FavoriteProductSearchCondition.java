package kr.or.hieating.favorite.dto;

import lombok.Getter;

@Getter
public class FavoriteProductSearchCondition {

  public static final int DEFAULT_SIZE = 12;

  private final Long userId;
  private final String sort;
  private final int page;
  private final int size;
  private final int offset;

  public FavoriteProductSearchCondition(Long userId, String sort, Integer page) {
    this.userId = userId;
    this.sort = normalizeSort(sort);
    this.page = Math.max(page == null ? 1 : page, 1);
    this.size = DEFAULT_SIZE;
    this.offset = (this.page - 1) * this.size;
  }

  private static String normalizeSort(String sort) {
    return switch (sort == null ? "latest" : sort) {
      case "popular", "priceAsc", "priceDesc" -> sort;
      default -> "latest";
    };
  }
}
