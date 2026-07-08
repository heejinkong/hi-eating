package kr.or.hieating.email.dto;

import java.time.LocalDateTime;
import kr.or.hieating.email.domain.EmailPublishStatus;
import kr.or.hieating.email.domain.EmailSendStatus;
import kr.or.hieating.email.domain.EmailValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDraftDto {

  private Long id;
  private Long hotDealId;
  private Long userId;
  private String recipientName;
  private String recipientEmail;
  private String hotDealTitle;
  private String subject;
  private String content;
  private EmailValidationStatus validationStatus;
  private String validationReason;
  private EmailSendStatus sendStatus;
  private EmailPublishStatus publishStatus;
  private String publishErrorMessage;
  private LocalDateTime publishedAt;
  private LocalDateTime hotDealCreatedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public String getValidationStatusLabel() {
    if (validationStatus == null) {
      return "검증 대기";
    }

    return switch (validationStatus) {
      case PASS -> "검증 통과";
      case FAIL -> "부적합";
      case PENDING -> "검증 대기";
    };
  }

  public String getValidationStatusClass() {
    if (validationStatus == null) {
      return "is-warning";
    }

    return switch (validationStatus) {
      case PASS -> "is-success";
      case FAIL -> "is-danger";
      case PENDING -> "is-warning";
    };
  }

  public String getPublishStatusLabel() {
    if (publishStatus == null) {
      return "관리자 검수 필요";
    }

    return switch (publishStatus) {
      case PENDING -> "관리자 검수 필요";
      case READY -> "발행 대기";
      case PUBLISHED -> "발행 완료";
      case SENDING -> "발송 중";
      case SENT -> "발송 완료";
      case RETRYING -> "재시도 대기";
      case FAILED -> "발행 실패";
    };
  }

  public String getPublishStatusClass() {
    if (publishStatus == null) {
      return "is-muted";
    }

    return switch (publishStatus) {
      case PENDING -> "is-muted";
      case READY -> "is-brand";
      case PUBLISHED -> "is-success";
      case SENDING -> "is-brand";
      case SENT -> "is-success";
      case RETRYING -> "is-warning";
      case FAILED -> "is-danger";
    };
  }

  public boolean isValidationFailed() {
    return sendStatus == EmailSendStatus.NEEDS_REVIEW
        || validationStatus == EmailValidationStatus.FAIL;
  }

  public boolean isPublishable() {
    return sendStatus == EmailSendStatus.APPROVED || publishStatus == EmailPublishStatus.READY;
  }
}
