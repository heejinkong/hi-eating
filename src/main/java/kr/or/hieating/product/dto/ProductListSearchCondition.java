package kr.or.hieating.product.dto;

import lombok.Getter;

@Getter
public class ProductListSearchCondition {

  public static final int DEFAULT_SIZE = 18;

  private final Long categoryId;
  private final Integer minPrice;
  private final Integer maxPrice;
  private final Integer minDiscountRate;
  private final String sort;
  private final int page;
  private final int size;
  private final int offset;

  public ProductListSearchCondition(
      Long categoryId,
      Integer minPrice,
      Integer maxPrice,
      Integer minDiscountRate,
      String sort,
      Integer page) {
    this.categoryId = categoryId;
    this.minPrice = normalizePositive(minPrice);
    this.maxPrice = normalizePositive(maxPrice);
    this.minDiscountRate = normalizePositive(minDiscountRate);
    this.sort = normalizeSort(sort);
    this.page = Math.max(page == null ? 1 : page, 1);
    this.size = DEFAULT_SIZE;
    this.offset = (this.page - 1) * this.size;
  }

  private static Integer normalizePositive(Integer value) {
    return value == null || value < 0 ? null : value;
  }

  private static String normalizeSort(String sort) {
    return switch (sort == null ? "popular" : sort) {
      case "latest", "priceAsc", "priceDesc" -> sort;
      default -> "popular";
    };
  }
}
