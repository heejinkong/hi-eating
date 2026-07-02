package kr.or.hieating.hotdeal.dto;

import java.util.List;

public record HotDealProductListPageResponseDto(
    List<HotDealProductListItemResponseDto> products, int page, int size, boolean hasMore) {

  public boolean hasProducts() {
    return products != null && !products.isEmpty();
  }
}
