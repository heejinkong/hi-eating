package kr.or.hieating.email.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEmailUpdateRequestDto {

  private String subject;
  private String content;
}
