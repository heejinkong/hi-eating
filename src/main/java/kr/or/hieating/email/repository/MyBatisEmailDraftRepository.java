package kr.or.hieating.email.repository;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.email.domain.EmailPublishStatus;
import kr.or.hieating.email.domain.EmailSendStatus;
import kr.or.hieating.email.domain.EmailValidationStatus;
import kr.or.hieating.email.dto.EmailDraftDto;
import kr.or.hieating.email.mapper.EmailSendLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MyBatisEmailDraftRepository implements EmailDraftRepository {

  private final EmailSendLogMapper emailSendLogMapper;

  @Override
  public List<EmailDraftDto> findAll() {
    return emailSendLogMapper.findReviewRequiredDrafts();
  }

  @Override
  public List<EmailDraftDto> findByValidationStatus(EmailValidationStatus validationStatus) {
    return findAll().stream()
        .filter(emailDraft -> emailDraft.getValidationStatus() == validationStatus)
        .toList();
  }

  @Override
  public List<EmailDraftDto> findByPublishStatus(EmailPublishStatus publishStatus) {
    return findAll().stream()
        .filter(emailDraft -> emailDraft.getPublishStatus() == publishStatus)
        .toList();
  }

  @Override
  public List<EmailDraftDto> findPublishReadyDrafts() {
    return emailSendLogMapper.findPublishReadyDrafts();
  }

  @Override
  public Optional<EmailDraftDto> findById(Long id) {
    return emailSendLogMapper.findById(id);
  }

  @Override
  public EmailDraftDto save(EmailDraftDto emailDraft) {
    throw new UnsupportedOperationException("EMAIL_SEND_LOGS 생성은 AI 이메일 생성 흐름에서 처리합니다.");
  }

  @Override
  public EmailDraftDto updateContent(Long id, String subject, String content) {
    int updated = emailSendLogMapper.updateContentAndApprove(id, subject, content);
    if (updated == 0) {
      throw new IllegalArgumentException("검증 필요 이메일만 관리자 수정이 가능합니다.");
    }
    return findRequired(id);
  }

  @Override
  public EmailDraftDto updateValidationResult(
      Long id, EmailValidationStatus validationStatus, String validationReason) {
    EmailSendStatus status =
        validationStatus == EmailValidationStatus.PASS
            ? EmailSendStatus.APPROVED
            : EmailSendStatus.NEEDS_REVIEW;
    return updateSendStatus(id, status, validationReason);
  }

  @Override
  public EmailDraftDto updatePublishStatus(
      Long id, EmailPublishStatus publishStatus, String publishErrorMessage) {
    EmailSendStatus status =
        switch (publishStatus) {
          case READY -> EmailSendStatus.APPROVED;
          case PUBLISHED -> EmailSendStatus.PUBLISHED;
          case SENDING -> EmailSendStatus.SENDING;
          case SENT -> EmailSendStatus.SENT;
          case RETRYING -> EmailSendStatus.RETRYING;
          case FAILED -> EmailSendStatus.FAILED;
          case PENDING -> EmailSendStatus.PENDING;
        };
    return updateSendStatus(id, status, publishErrorMessage);
  }

  public EmailDraftDto updateSendStatus(Long id, EmailSendStatus sendStatus, String failureReason) {
    int updated = emailSendLogMapper.updateStatus(id, sendStatus.name(), failureReason);
    if (updated == 0) {
      throw new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다.");
    }
    return findRequired(id);
  }

  private EmailDraftDto findRequired(Long id) {
    return findById(id).orElseThrow(() -> new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다."));
  }
}
