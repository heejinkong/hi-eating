package kr.or.hieating.email.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminEmailPublishFailureDto {

  private Long emailDraftId;
  private String reason;
}
