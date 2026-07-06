package kr.or.hieating.email.admin.dto;

import java.util.List;
import kr.or.hieating.email.dto.EmailDraftDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminEmailDashboardDto {

  private List<EmailDraftDto> emailDrafts;
  private EmailDraftDto selectedEmailDraft;
  private AdminEmailSummaryDto summary;

  public boolean hasEmailDrafts() {
    return emailDrafts != null && !emailDrafts.isEmpty();
  }
}
