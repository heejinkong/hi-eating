package kr.or.hieating.ai.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.or.hieating.ai.dto.TargetSelectionJobDto;
import kr.or.hieating.ai.dto.TargetSelectionResult;
import kr.or.hieating.ai.mapper.TargetSelectionJobMapper;
import org.junit.jupiter.api.Test;

class TargetSelectionJobProcessorTest {

  private final TargetSelectionJobMapper jobMapper = mock(TargetSelectionJobMapper.class);
  private final TargetUserSelectionAiService selectionService =
      mock(TargetUserSelectionAiService.class);
  private final HotDealEmailGenerationService emailGenerationService =
      mock(HotDealEmailGenerationService.class);
  private final TargetSelectionJobProcessor processor =
      new TargetSelectionJobProcessor(jobMapper, selectionService, emailGenerationService);

  @Test
  void completesClaimedJobAfterSelectionSucceeds() {
    TargetSelectionJobDto job = new TargetSelectionJobDto(1L, 100L, "PENDING", 0, 3);
    when(jobMapper.findNextRunnableJob()).thenReturn(job);
    when(jobMapper.claimJob(1L)).thenReturn(1);
    when(selectionService.selectAndSaveTargets(100L))
        .thenReturn(new TargetSelectionResult(100L, 10, 4, 4));

    processor.processNextJob();

    verify(emailGenerationService).generateAndSave(100L);
    verify(jobMapper).markCompleted(1L, 10, 4, 4);
    verify(jobMapper, never())
        .markFailed(
            org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void skipsEmailGenerationWhenNoUserIsSelected() {
    TargetSelectionJobDto job = new TargetSelectionJobDto(3L, 300L, "PENDING", 0, 3);
    when(jobMapper.findNextRunnableJob()).thenReturn(job);
    when(jobMapper.claimJob(3L)).thenReturn(1);
    when(selectionService.selectAndSaveTargets(300L))
        .thenReturn(new TargetSelectionResult(300L, 10, 0, 0));

    processor.processNextJob();

    verify(emailGenerationService, never()).generateAndSave(300L);
    verify(jobMapper).markCompleted(3L, 10, 0, 0);
  }

  @Test
  void marksClaimedJobAsFailedWhenSelectionThrows() {
    TargetSelectionJobDto job = new TargetSelectionJobDto(2L, 200L, "PENDING", 0, 3);
    when(jobMapper.findNextRunnableJob()).thenReturn(job);
    when(jobMapper.claimJob(2L)).thenReturn(1);
    when(selectionService.selectAndSaveTargets(200L))
        .thenThrow(new IllegalStateException("Ollama timeout"));

    processor.processNextJob();

    verify(jobMapper).markFailed(2L, "Ollama timeout");
    verify(jobMapper, never())
        .markCompleted(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.anyInt());
  }
}
