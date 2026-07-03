package kr.or.hieating.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserDto {
  private final Long id;
  private final String name;
  private final String email;
}
