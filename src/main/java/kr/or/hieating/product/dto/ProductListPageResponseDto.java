package kr.or.hieating.product.dto;

import java.util.List;

public record ProductListPageResponseDto(
    List<ProductListItemResponseDto> products, int page, int size, int totalCount, int totalPages) {

  private static final int PAGE_BLOCK_SIZE = 10;

  public boolean hasProducts() {
    return products != null && !products.isEmpty();
  }

  public boolean hasPrevious() {
    return page > 1;
  }

  public boolean hasNext() {
    return page < totalPages;
  }

  public int startPage() {
    return ((page - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
  }

  public int endPage() {
    return Math.min(startPage() + PAGE_BLOCK_SIZE - 1, totalPages);
  }

  public boolean hasPreviousBlock() {
    return startPage() > 1;
  }

  public boolean hasNextBlock() {
    return endPage() < totalPages;
  }

  public int previousBlockPage() {
    return Math.max(startPage() - 1, 1);
  }

  public int nextBlockPage() {
    return Math.min(endPage() + 1, totalPages);
  }
}
