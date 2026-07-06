package kr.or.hieating.ai.service;

import java.util.List;
import kr.or.hieating.ai.dto.TargetSelectionEvaluationDto;
import kr.or.hieating.ai.dto.TargetUserDto;
import kr.or.hieating.ai.mapper.EmailSendLogMapper;
import kr.or.hieating.ai.mapper.TargetSelectionHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class TargetSelectionPersistenceService {

  private final TargetSelectionHistoryMapper historyMapper;
  private final EmailSendLogMapper emailSendLogMapper;

  @Transactional
  public int save(
      long hotDealId, List<TargetSelectionEvaluationDto> evaluations, List<TargetUserDto> targets) {
    if (!evaluations.isEmpty()) {
      historyMapper.upsertEvaluations(hotDealId, evaluations);
    }
    return targets.isEmpty() ? 0 : emailSendLogMapper.insertPendingLogs(hotDealId, targets);
  }
}
