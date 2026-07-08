package kr.or.hieating.email.publisher;

import java.util.List;
import kr.or.hieating.email.config.EmailEventProperties;
import kr.or.hieating.email.repository.EmailDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "greenfood.email-event.auto-publish",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class EmailPublishScheduler {

  private final EmailDraftRepository emailDraftRepository;
  private final EmailPublisher emailPublisher;
  private final EmailEventProperties emailEventProperties;

  @Value("${greenfood.email-event.auto-publish.batch-size:50}")
  private int batchSize;

  @Scheduled(fixedDelayString = "${greenfood.email-event.auto-publish.fixed-delay:5s}")
  public void publishApprovedEmails() {

    if (!emailEventProperties.enabled()) {
      return;
    }

    List<Long> emailDraftIds = emailDraftRepository.findPublishReadyDraftIds(batchSize);

    if (emailDraftIds.isEmpty()) {
      return;
    }

    int publishedCount = 0;
    int failedCount = 0;

    for (Long emailDraftId : emailDraftIds) {
      try {
        emailPublisher.publish(emailDraftId);
        publishedCount++;
      } catch (IllegalArgumentException exception) {
        log.debug(
            "[Email Auto Publish] skipped emailDraftId={} reason={}",
            emailDraftId,
            exception.getMessage());
      } catch (RuntimeException exception) {
        failedCount++;
        log.error("[Email Auto Publish] failed emailDraftId={}", emailDraftId, exception);
      }
    }
  }
}
