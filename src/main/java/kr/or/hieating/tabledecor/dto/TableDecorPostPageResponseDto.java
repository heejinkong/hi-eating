package kr.or.hieating.tabledecor.dto;

import java.util.List;

public record TableDecorPostPageResponseDto(
    List<TableDecorPostListItemDto> posts, int page, int size, int totalCount, int totalPages) {

  public boolean hasPosts() {
    return posts != null && !posts.isEmpty();
  }

  public boolean hasPrevious() {
    return page > 1;
  }

  public boolean hasNext() {
    return page < totalPages;
  }
}
