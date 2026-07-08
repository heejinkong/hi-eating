package kr.or.hieating.hotdeal.dto;

import java.util.List;

public record HotDealProductListPageResponseDto(
    List<HotDealProductListItemResponseDto> products,
    int page,
    int size,
    int totalCount,
    int totalPages,
    boolean hasMore) {

  public HotDealProductListPageResponseDto(
      List<HotDealProductListItemResponseDto> products,
      int page,
      int size,
      int totalCount,
      int totalPages) {
    this(products, page, size, totalCount, totalPages, page < totalPages);
  }

  public boolean hasProducts() {
    return products != null && !products.isEmpty();
  }
}
