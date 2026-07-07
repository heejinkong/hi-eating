package kr.or.hieating.ai.service;

import kr.or.hieating.ai.dto.TargetSelectionJobDto;
import kr.or.hieating.ai.dto.TargetSelectionResult;
import kr.or.hieating.ai.mapper.TargetSelectionJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class TargetSelectionJobProcessor {

  private static final int MAX_FAILURE_REASON_LENGTH = 3500;

  private final TargetSelectionJobMapper jobMapper;
  private final TargetUserSelectionAiService selectionService;
  private final HotDealEmailGenerationService emailGenerationService;
  private final HotDealEmailQualityValidationService emailQualityValidationService;

  public void processNextJob() {
    TargetSelectionJobDto job = jobMapper.findNextRunnableJob();
    if (job == null || jobMapper.claimJob(job.id()) != 1) {
      return;
    }

    try {
      TargetSelectionResult result = selectionService.selectAndSaveTargets(job.hotDealId());
      if (result.selectedCount() > 0) {
        var email = emailGenerationService.generateAndSave(job.hotDealId());
        emailQualityValidationService.validateAndApply(job.hotDealId(), email);
      }
      jobMapper.markCompleted(
          job.id(), result.candidateCount(), result.selectedCount(), result.insertedCount());
      log.info(
          "[대상선정 Job] 완료 jobId={} hotDealId={} 후보={} 선정={} 저장={}",
          job.id(),
          job.hotDealId(),
          result.candidateCount(),
          result.selectedCount(),
          result.insertedCount());
    } catch (RuntimeException exception) {
      String failureReason = abbreviate(buildFailureReason(exception));
      jobMapper.markFailed(job.id(), failureReason);
      log.error(
          "[대상선정 Job] 실패 jobId={} hotDealId={} retryCount={}/{}",
          job.id(),
          job.hotDealId(),
          job.retryCount() + 1,
          job.maxRetryCount(),
          exception);
    }
  }

  public int recoverInterruptedJobs() {
    return jobMapper.recoverInterruptedJobs();
  }

  private String abbreviate(String message) {
    String value = message == null ? "원인을 확인할 수 없는 오류" : message;
    return value.length() <= MAX_FAILURE_REASON_LENGTH
        ? value
        : value.substring(0, MAX_FAILURE_REASON_LENGTH);
  }

  private String buildFailureReason(Throwable exception) {
    StringBuilder reason = new StringBuilder();
    Throwable current = exception;
    while (current != null) {
      String message = current.getMessage();
      if (message != null && !message.isBlank() && reason.indexOf(message) < 0) {
        if (!reason.isEmpty()) {
          reason.append(" | 원인: ");
        }
        reason.append(message);
      }
      current = current.getCause();
    }
    return reason.isEmpty() ? "원인을 확인할 수 없는 오류" : reason.toString();
  }
}
