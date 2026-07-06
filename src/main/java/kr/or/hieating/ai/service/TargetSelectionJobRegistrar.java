package kr.or.hieating.ai.service;

import kr.or.hieating.ai.config.AiProperties;
import kr.or.hieating.ai.mapper.TargetSelectionJobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class TargetSelectionJobRegistrar {

  private final TargetSelectionJobMapper jobMapper;
  private final AiProperties properties;

  public void register(long hotDealId) {
    jobMapper.insertPendingJob(hotDealId, properties.targetSelection().jobMaxRetries());
  }
}
