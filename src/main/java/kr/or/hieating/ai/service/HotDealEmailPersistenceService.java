package kr.or.hieating.ai.service;

import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.mapper.HotDealEmailContentMapper;
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
public class HotDealEmailPersistenceService {

  private final HotDealEmailContentMapper contentMapper;

  @Transactional
  public int saveAndApply(long hotDealId, GeneratedHotDealEmailDto email) {
    contentMapper.upsertGeneratedContent(hotDealId, email.subject(), email.content());
    return contentMapper.applyContentToSendLogs(hotDealId, email.subject(), email.content());
  }

  @Transactional
  public int applyExisting(long hotDealId, GeneratedHotDealEmailDto email) {
    return contentMapper.applyContentToSendLogs(hotDealId, email.subject(), email.content());
  }
}
