package kr.or.hieating.product.dto;

import java.util.List;

public record ProductListPageResponseDto(
    List<ProductListItemResponseDto> products, int page, int size, int totalCount, int totalPages) {

  public boolean hasProducts() {
    return products != null && !products.isEmpty();
  }

  public boolean hasPrevious() {
    return page > 1;
  }

  public boolean hasNext() {
    return page < totalPages;
  }
}
