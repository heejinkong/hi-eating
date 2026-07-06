package kr.or.hieating.ai.scheduler;

import kr.or.hieating.ai.service.TargetSelectionJobProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class TargetSelectionJobScheduler {

  private final TargetSelectionJobProcessor jobProcessor;

  @EventListener(ApplicationReadyEvent.class)
  public void recoverInterruptedJobs() {
    int recoveredCount = jobProcessor.recoverInterruptedJobs();
    if (recoveredCount > 0) {
      log.warn("[대상선정 Job] 애플리케이션 재시작으로 중단된 작업 {}건을 재시도 대기로 복구했습니다.", recoveredCount);
    }
  }

  @Scheduled(fixedDelayString = "${greenfood.ai.target-selection.job-poll-delay:5s}")
  public void processNextJob() {
    jobProcessor.processNextJob();
  }
}
