package kr.or.hieating.email.publisher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import kr.or.hieating.email.config.EmailEventProperties;
import kr.or.hieating.email.domain.EmailPublishStatus;
import kr.or.hieating.email.domain.EmailSendStatus;
import kr.or.hieating.email.dto.EmailDraftDto;
import kr.or.hieating.email.repository.EmailDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailPublisher {

  private static final Duration DEFAULT_CONFIRM_TIMEOUT = Duration.ofSeconds(5);

  private final RabbitTemplate rabbitTemplate;
  private final EmailEventProperties emailEventProperties;
  private final EmailDraftRepository emailDraftRepository;

  public EmailPublishMessage publish(Long emailDraftId) {
    if (!emailEventProperties.enabled()) {
      throw new IllegalStateException("Email event publishing is disabled.");
    }

    EmailDraftDto emailDraft =
        emailDraftRepository
            .findById(emailDraftId)
            .orElseThrow(() -> new IllegalArgumentException("Email draft was not found."));

    if (emailDraft.getSendStatus() != EmailSendStatus.APPROVED) {
      throw new IllegalArgumentException("Only approved emails can be published to RabbitMQ.");
    }

    EmailPublishMessage message = createMessage(emailDraft);
    CorrelationData correlationData = new CorrelationData(String.valueOf(emailDraftId));

    try {
      emailDraftRepository.updatePublishStatus(emailDraftId, EmailPublishStatus.PUBLISHED, null);
      rabbitTemplate.convertAndSend(
          emailEventProperties.exchange(),
          emailEventProperties.routingKey(),
          message,
          rabbitMessage -> {
            rabbitMessage.getMessageProperties().setCorrelationId(String.valueOf(emailDraftId));
            return rabbitMessage;
          },
          correlationData);
      verifyBrokerAcceptedMessage(emailDraftId, correlationData);
      return message;
    } catch (AmqpException exception) {
      emailDraftRepository.updatePublishStatus(
          emailDraftId, EmailPublishStatus.FAILED, exception.getMessage());
      throw exception;
    }
  }

  private void verifyBrokerAcceptedMessage(Long emailDraftId, CorrelationData correlationData) {
    try {
      CorrelationData.Confirm confirm =
          correlationData
              .getFuture()
              .get(publishConfirmTimeout().toMillis(), TimeUnit.MILLISECONDS);

      ReturnedMessage returnedMessage = correlationData.getReturned();
      if (returnedMessage != null) {
        throw new AmqpException(
            "RabbitMQ routing failed. replyCode="
                + returnedMessage.getReplyCode()
                + ", replyText="
                + returnedMessage.getReplyText());
      }

      if (!confirm.isAck()) {
        throw new AmqpException(
            "RabbitMQ broker rejected message. reason="
                + (confirm.getReason() == null ? "unknown" : confirm.getReason()));
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AmqpException(
          "RabbitMQ publish confirm wait interrupted. emailDraftId=" + emailDraftId, exception);
    } catch (ExecutionException exception) {
      throw new AmqpException(
          "RabbitMQ publish confirm failed. emailDraftId=" + emailDraftId, exception);
    } catch (TimeoutException exception) {
      throw new AmqpException(
          "RabbitMQ publish confirm timed out. emailDraftId=" + emailDraftId, exception);
    }
  }

  private Duration publishConfirmTimeout() {
    return emailEventProperties.publishConfirmTimeout() == null
        ? DEFAULT_CONFIRM_TIMEOUT
        : emailEventProperties.publishConfirmTimeout();
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
