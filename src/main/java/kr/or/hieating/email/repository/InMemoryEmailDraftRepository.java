package kr.or.hieating.email.repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kr.or.hieating.email.domain.EmailPublishStatus;
import kr.or.hieating.email.domain.EmailSendStatus;
import kr.or.hieating.email.domain.EmailValidationStatus;
import kr.or.hieating.email.dto.EmailDraftDto;

public class InMemoryEmailDraftRepository implements EmailDraftRepository {

  private final AtomicLong sequence = new AtomicLong(1);
  private final Map<Long, EmailDraftDto> storage = new ConcurrentHashMap<>();

  public InMemoryEmailDraftRepository() {
    save(
        EmailDraftDto.builder()
            .hotDealId(1L)
            .userId(11L)
            .recipientName("김하은")
            .recipientEmail("haeun@example.com")
            .hotDealTitle("Kids Side Dish Deal")
            .subject("[Hi Eating] 하은님을 위한 오늘의 키즈 반찬 핫딜")
            .content(
                """
                하은님, 최근 즐겨찾기한 아이 반찬 상품과 구매 이력을 바탕으로 오늘의 핫딜을 추천드려요.

                이번 Kids Side Dish Deal에서는 인기 반찬 구성을 더 합리적인 가격으로 만나볼 수 있습니다.

                필요한 상품을 확인하고 오늘 종료 전 혜택을 살펴보세요.
                """)
            .validationStatus(EmailValidationStatus.FAIL)
            .validationReason("과장 표현 확인 필요")
            .publishStatus(EmailPublishStatus.READY)
            .build());
    save(
        EmailDraftDto.builder()
            .hotDealId(2L)
            .userId(12L)
            .recipientName("박준호")
            .recipientEmail("junho@example.com")
            .hotDealTitle("Protein Meal Deal")
            .subject("[Hi Eating] 준호님이 관심 가질 단백질 식단 핫딜")
            .content("준호님이 자주 구매한 단백질 식단과 연관된 핫딜을 안내드립니다.")
            .validationStatus(EmailValidationStatus.FAIL)
            .validationReason("추천 근거 부족")
            .publishStatus(EmailPublishStatus.READY)
            .build());
    save(
        EmailDraftDto.builder()
            .hotDealId(3L)
            .userId(13L)
            .recipientName("이서연")
            .recipientEmail("seoyeon@example.com")
            .hotDealTitle("Fresh Morning Deal")
            .subject("[Hi Eating] 서연님을 위한 아침 식단 핫딜")
            .content("서연님이 즐겨찾기한 신선식품 기반으로 오늘의 핫딜을 추천드립니다.")
            .validationStatus(EmailValidationStatus.PASS)
            .validationReason(null)
            .publishStatus(EmailPublishStatus.READY)
            .build());
  }

  @Override
  public List<EmailDraftDto> findAll() {
    return storage.values().stream()
        .sorted(Comparator.comparing(EmailDraftDto::getCreatedAt).reversed())
        .map(this::copyOf)
        .toList();
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
    return findAll().stream()
        .filter(emailDraft -> emailDraft.getValidationStatus() == EmailValidationStatus.PASS)
        .filter(emailDraft -> emailDraft.getPublishStatus() == EmailPublishStatus.READY)
        .toList();
  }

  @Override
  public List<Long> findPublishReadyDraftIds(int limit) {
    return findPublishReadyDrafts().stream().limit(limit).map(EmailDraftDto::getId).toList();
  }

  @Override
  public Optional<EmailDraftDto> findById(Long id) {
    return Optional.ofNullable(storage.get(id)).map(this::copyOf);
  }

  @Override
  public EmailDraftDto save(EmailDraftDto emailDraft) {
    LocalDateTime now = LocalDateTime.now();
    EmailDraftDto saved = copyOf(emailDraft);
    if (saved.getId() == null) {
      saved.setId(sequence.getAndIncrement());
      saved.setCreatedAt(now);
    }
    if (saved.getValidationStatus() == null) {
      saved.setValidationStatus(EmailValidationStatus.PENDING);
    }
    if (saved.getPublishStatus() == null) {
      saved.setPublishStatus(EmailPublishStatus.READY);
    }
    if (saved.getSendStatus() == null) {
      saved.setSendStatus(toSendStatus(saved.getPublishStatus()));
    }
    if (saved.getCreatedAt() == null) {
      saved.setCreatedAt(now);
    }
    saved.setUpdatedAt(now);
    storage.put(saved.getId(), saved);
    return copyOf(saved);
  }

  @Override
  public EmailDraftDto updateContent(Long id, String subject, String content) {
    return updateRequired(
        id,
        emailDraft -> {
          emailDraft.setSubject(subject);
          emailDraft.setContent(content);
        });
  }

  @Override
  public EmailDraftDto updateValidationResult(
      Long id, EmailValidationStatus validationStatus, String validationReason) {
    return updateRequired(
        id,
        emailDraft -> {
          emailDraft.setValidationStatus(validationStatus);
          emailDraft.setValidationReason(validationReason);
        });
  }

  @Override
  public EmailDraftDto updatePublishStatus(
      Long id, EmailPublishStatus publishStatus, String publishErrorMessage) {
    return updateRequired(
        id,
        emailDraft -> {
          emailDraft.setPublishStatus(publishStatus);
          emailDraft.setSendStatus(toSendStatus(publishStatus));
          emailDraft.setPublishErrorMessage(publishErrorMessage);
          emailDraft.setPublishedAt(
              publishStatus == EmailPublishStatus.PUBLISHED ? LocalDateTime.now() : null);
        });
  }

  @Override
  public boolean claimForPublishing(Long id) {
    EmailDraftDto emailDraft = storage.get(id);
    if (emailDraft == null || emailDraft.getPublishStatus() != EmailPublishStatus.READY) {
      return false;
    }

    updatePublishStatus(id, EmailPublishStatus.PUBLISHING, null);
    return true;
  }

  private EmailSendStatus toSendStatus(EmailPublishStatus publishStatus) {
    return switch (publishStatus) {
      case READY -> EmailSendStatus.APPROVED;
      case PUBLISHING -> EmailSendStatus.PUBLISHING;
      case PUBLISHED -> EmailSendStatus.PUBLISHED;
      case SENDING -> EmailSendStatus.SENDING;
      case SENT -> EmailSendStatus.SENT;
      case RETRYING -> EmailSendStatus.RETRYING;
      case FAILED -> EmailSendStatus.FAILED;
      case PENDING -> EmailSendStatus.PENDING;
    };
  }

  private EmailDraftDto updateRequired(Long id, EmailDraftUpdater updater) {
    EmailDraftDto updated =
        storage.compute(
            id,
            (key, existing) -> {
              if (existing == null) {
                throw new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다.");
              }
              EmailDraftDto copy = copyOf(existing);
              updater.update(copy);
              copy.setUpdatedAt(LocalDateTime.now());
              return copy;
            });
    return copyOf(updated);
  }

  private EmailDraftDto copyOf(EmailDraftDto source) {
    if (source == null) {
      return null;
    }
    return EmailDraftDto.builder()
        .id(source.getId())
        .hotDealId(source.getHotDealId())
        .userId(source.getUserId())
        .recipientName(source.getRecipientName())
        .recipientEmail(source.getRecipientEmail())
        .hotDealTitle(source.getHotDealTitle())
        .subject(source.getSubject())
        .content(source.getContent())
        .validationStatus(source.getValidationStatus())
        .validationReason(source.getValidationReason())
        .sendStatus(source.getSendStatus())
        .publishStatus(source.getPublishStatus())
        .publishErrorMessage(source.getPublishErrorMessage())
        .publishedAt(source.getPublishedAt())
        .createdAt(source.getCreatedAt())
        .updatedAt(source.getUpdatedAt())
        .build();
  }

  @FunctionalInterface
  private interface EmailDraftUpdater {
    void update(EmailDraftDto emailDraft);
  }
}
