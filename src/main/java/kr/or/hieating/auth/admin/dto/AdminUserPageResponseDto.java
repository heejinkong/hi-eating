package kr.or.hieating.auth.admin.dto;

import java.util.List;

public record AdminUserPageResponseDto(
    List<AdminUserRoleTargetDto> users, int page, int size, int totalCount, int totalPages) {

  public boolean hasPrevious() {
    return page > 1;
  }

  public boolean hasNext() {
    return page < totalPages;
  }
}
