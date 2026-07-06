package kr.or.hieating.email.admin.dto;

import java.util.List;
import kr.or.hieating.email.dto.EmailDraftDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminEmailPublishBatchResponseDto {

  private List<EmailDraftDto> publishedEmails;
  private List<AdminEmailPublishFailureDto> failedEmails;

  public int getPublishedCount() {
    return publishedEmails == null ? 0 : publishedEmails.size();
  }

  public int getFailedCount() {
    return failedEmails == null ? 0 : failedEmails.size();
  }

  public boolean isAllFailed() {
    return getPublishedCount() == 0 && getFailedCount() > 0;
  }
}
