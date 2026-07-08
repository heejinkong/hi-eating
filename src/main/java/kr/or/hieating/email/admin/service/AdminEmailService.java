package kr.or.hieating.email.admin.service;

import java.util.ArrayList;
import java.util.List;
import kr.or.hieating.email.admin.dto.AdminEmailDashboardDto;
import kr.or.hieating.email.admin.dto.AdminEmailPublishBatchResponseDto;
import kr.or.hieating.email.admin.dto.AdminEmailPublishFailureDto;
import kr.or.hieating.email.admin.dto.AdminEmailPublishRequestDto;
import kr.or.hieating.email.admin.dto.AdminEmailSummaryDto;
import kr.or.hieating.email.admin.dto.AdminEmailUpdateRequestDto;
import kr.or.hieating.email.domain.EmailPublishStatus;
import kr.or.hieating.email.domain.EmailSendStatus;
import kr.or.hieating.email.domain.EmailValidationStatus;
import kr.or.hieating.email.dto.EmailDraftDto;
import kr.or.hieating.email.publisher.EmailPublisher;
import kr.or.hieating.email.repository.EmailDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminEmailService {

  private final EmailDraftRepository emailDraftRepository;
  private final EmailPublisher emailPublisher;

  public AdminEmailDashboardDto getDashboard(Long selectedEmailDraftId) {
    List<EmailDraftDto> emailDrafts = emailDraftRepository.findAll();
    EmailDraftDto selectedEmailDraft = findSelectedEmailDraft(emailDrafts, selectedEmailDraftId);

    return new AdminEmailDashboardDto(emailDrafts, selectedEmailDraft, createSummary(emailDrafts));
  }

  public EmailDraftDto getEmailDraft(Long emailDraftId) {
    if (emailDraftId == null) {
      throw new IllegalArgumentException("이메일 발송 후보를 선택해주세요.");
    }

    return emailDraftRepository
        .findById(emailDraftId)
        .orElseThrow(() -> new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다."));
  }

  public EmailDraftDto updateFailedEmailContent(
      Long emailDraftId, AdminEmailUpdateRequestDto request) {
    if (emailDraftId == null) {
      throw new IllegalArgumentException("이메일 발송 후보를 선택해주세요.");
    }

    String subject = normalizeRequiredText(request == null ? null : request.getSubject(), "제목");
    String content = normalizeRequiredText(request == null ? null : request.getContent(), "본문");

    EmailDraftDto emailDraft =
        emailDraftRepository
            .findById(emailDraftId)
            .orElseThrow(() -> new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다."));

    if (emailDraft.getValidationStatus() != EmailValidationStatus.FAIL) {
      throw new IllegalArgumentException("검증 필요 이메일만 관리자 수정이 가능합니다.");
    }

    return emailDraftRepository.updateContent(emailDraftId, subject, content);
  }

  public EmailDraftDto publishEmailDraft(Long emailDraftId) {
    EmailDraftDto emailDraft = validatePublishTarget(emailDraftId);
    if (emailDraft.getSendStatus() == EmailSendStatus.NEEDS_REVIEW) {
      emailDraftRepository.updateValidationResult(emailDraftId, EmailValidationStatus.PASS, null);
    }
    emailPublisher.publish(emailDraftId);

    return emailDraftRepository
        .findById(emailDraftId)
        .orElseThrow(() -> new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다."));
  }

  public AdminEmailPublishBatchResponseDto publishEmailDrafts(AdminEmailPublishRequestDto request) {
    if (request == null
        || request.getEmailDraftIds() == null
        || request.getEmailDraftIds().isEmpty()) {
      throw new IllegalArgumentException("발송할 이메일을 선택해주세요.");
    }

    return publishEmailDrafts(request.getEmailDraftIds());
  }

  public AdminEmailPublishBatchResponseDto publishValidationPassedReadyEmails() {
    List<EmailDraftDto> publishReadyDrafts = emailDraftRepository.findPublishReadyDrafts();

    if (publishReadyDrafts.isEmpty()) {
      throw new IllegalArgumentException("자동 발송할 검증 통과 이메일이 없습니다.");
    }

    return publishEmailDrafts(publishReadyDrafts.stream().map(EmailDraftDto::getId).toList());
  }

  private AdminEmailPublishBatchResponseDto publishEmailDrafts(List<Long> emailDraftIds) {
    List<EmailDraftDto> publishedEmails = new ArrayList<>();
    List<AdminEmailPublishFailureDto> failedEmails = new ArrayList<>();

    for (Long emailDraftId : emailDraftIds) {
      try {
        publishedEmails.add(publishEmailDraft(emailDraftId));
      } catch (IllegalArgumentException exception) {
        failedEmails.add(new AdminEmailPublishFailureDto(emailDraftId, exception.getMessage()));
      }
    }

    return new AdminEmailPublishBatchResponseDto(publishedEmails, failedEmails);
  }

  private EmailDraftDto findSelectedEmailDraft(
      List<EmailDraftDto> emailDrafts, Long selectedEmailDraftId) {
    if (emailDrafts.isEmpty()) {
      return null;
    }

    if (selectedEmailDraftId == null) {
      return emailDrafts.get(0);
    }

    return emailDrafts.stream()
        .filter(emailDraft -> selectedEmailDraftId.equals(emailDraft.getId()))
        .findFirst()
        .orElse(emailDrafts.get(0));
  }

  private AdminEmailSummaryDto createSummary(List<EmailDraftDto> emailDrafts) {
    long validationPassCount = countValidationStatus(emailDrafts, EmailValidationStatus.PASS);
    long validationFailCount = countValidationStatus(emailDrafts, EmailValidationStatus.FAIL);
    long publishedCount =
        emailDrafts.stream()
            .filter(emailDraft -> isPublishedFlow(emailDraft.getSendStatus()))
            .count();
    long readyCount = countPublishStatus(emailDrafts, EmailPublishStatus.READY);

    return new AdminEmailSummaryDto(
        validationPassCount, validationFailCount, publishedCount, readyCount);
  }

  private long countValidationStatus(
      List<EmailDraftDto> emailDrafts, EmailValidationStatus validationStatus) {
    return emailDrafts.stream()
        .filter(emailDraft -> emailDraft.getValidationStatus() == validationStatus)
        .count();
  }

  private long countPublishStatus(
      List<EmailDraftDto> emailDrafts, EmailPublishStatus publishStatus) {
    return emailDrafts.stream()
        .filter(emailDraft -> emailDraft.getPublishStatus() == publishStatus)
        .count();
  }

  private boolean isPublishedFlow(EmailSendStatus sendStatus) {
    return sendStatus == EmailSendStatus.PUBLISHING
        || sendStatus == EmailSendStatus.PUBLISHED
        || sendStatus == EmailSendStatus.SENDING
        || sendStatus == EmailSendStatus.SENT
        || sendStatus == EmailSendStatus.RETRYING;
  }

  private String normalizeRequiredText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + "을(를) 입력해주세요.");
    }

    return value.trim();
  }

  private EmailDraftDto validatePublishTarget(Long emailDraftId) {
    if (emailDraftId == null) {
      throw new IllegalArgumentException("이메일 발송 후보를 선택해주세요.");
    }

    EmailDraftDto emailDraft =
        emailDraftRepository
            .findById(emailDraftId)
            .orElseThrow(() -> new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다."));

    if (emailDraft.getSendStatus() != EmailSendStatus.APPROVED
        && emailDraft.getSendStatus() != EmailSendStatus.NEEDS_REVIEW) {
      throw new IllegalArgumentException("관리자 검수 필요 또는 승인 상태의 이메일만 발행할 수 있습니다.");
    }

    normalizeRequiredText(emailDraft.getRecipientEmail(), "수신자");
    normalizeRequiredText(emailDraft.getSubject(), "제목");
    normalizeRequiredText(emailDraft.getContent(), "본문");

    return emailDraft;
  }
}
