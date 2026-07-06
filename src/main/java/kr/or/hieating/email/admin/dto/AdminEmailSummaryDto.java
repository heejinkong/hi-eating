package kr.or.hieating.email.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminEmailSummaryDto {

  private long validationPassCount;
  private long validationFailCount;
  private long publishedCount;
  private long readyCount;
}
