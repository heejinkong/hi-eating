package kr.or.hieating.email.publisher;

import java.time.LocalDateTime;
import kr.or.hieating.email.config.EmailEventProperties;
import kr.or.hieating.email.domain.EmailPublishStatus;
import kr.or.hieating.email.domain.EmailSendStatus;
import kr.or.hieating.email.dto.EmailDraftDto;
import kr.or.hieating.email.repository.EmailDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailPublisher {

  private final RabbitTemplate rabbitTemplate;
  private final EmailEventProperties emailEventProperties;
  private final EmailDraftRepository emailDraftRepository;

  public EmailPublishMessage publish(Long emailDraftId) {
    if (!emailEventProperties.enabled()) {
      throw new IllegalStateException("이메일 이벤트 발행이 비활성화되어 있습니다.");
    }

    EmailDraftDto emailDraft =
        emailDraftRepository
            .findById(emailDraftId)
            .orElseThrow(() -> new IllegalArgumentException("이메일 발송 후보를 찾을 수 없습니다."));

    if (emailDraft.getSendStatus() != EmailSendStatus.APPROVED) {
      throw new IllegalArgumentException("승인된 이메일만 RabbitMQ로 발행할 수 있습니다.");
    }

    EmailPublishMessage message = createMessage(emailDraft);

    try {
      emailDraftRepository.updatePublishStatus(emailDraftId, EmailPublishStatus.PUBLISHED, null);
      rabbitTemplate.convertAndSend(
          emailEventProperties.exchange(), emailEventProperties.routingKey(), message);
      return message;
    } catch (AmqpException exception) {
      emailDraftRepository.updatePublishStatus(
          emailDraftId, EmailPublishStatus.FAILED, exception.getMessage());
      throw exception;
    }
  }

  private EmailPublishMessage createMessage(EmailDraftDto emailDraft) {
    return new EmailPublishMessage(
        emailDraft.getId(),
        emailDraft.getHotDealId(),
        emailDraft.getUserId(),
        emailDraft.getRecipientName(),
        emailDraft.getRecipientEmail(),
        emailDraft.getHotDealTitle(),
        emailDraft.getSubject(),
        emailDraft.getContent(),
        LocalDateTime.now());
  }
}
