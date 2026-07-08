package kr.or.hieating.email.repository;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.email.domain.EmailPublishStatus;
import kr.or.hieating.email.domain.EmailValidationStatus;
import kr.or.hieating.email.dto.EmailDraftDto;

public interface EmailDraftRepository {

  List<EmailDraftDto> findAll();

  List<EmailDraftDto> findByValidationStatus(EmailValidationStatus validationStatus);

  List<EmailDraftDto> findByPublishStatus(EmailPublishStatus publishStatus);

  List<EmailDraftDto> findPublishReadyDrafts();

  List<Long> findPublishReadyDraftIds(int limit);

  Optional<EmailDraftDto> findById(Long id);

  EmailDraftDto save(EmailDraftDto emailDraft);

  EmailDraftDto updateContent(Long id, String subject, String content);

  EmailDraftDto updateValidationResult(
      Long id, EmailValidationStatus validationStatus, String validationReason);

  EmailDraftDto updatePublishStatus(
      Long id, EmailPublishStatus publishStatus, String publishErrorMessage);

  boolean claimForPublishing(Long id);
}
