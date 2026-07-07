package kr.or.hieating.ai.service;

import kr.or.hieating.ai.dto.EmailQualityValidationResult;
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

  @Transactional
  public int applyValidation(long hotDealId, EmailQualityValidationResult result) {
    String status = result.isPass() ? "PASS" : "FAIL";
    String reason = result.reason();
    if (contentMapper.updateValidationResult(hotDealId, status, reason) != 1) {
      throw new IllegalStateException("검증 결과를 저장할 이메일 콘텐츠가 없습니다. hotDealId=" + hotDealId);
    }
    contentMapper.applyValidationToSendLogs(hotDealId, status, reason);

    int totalCount = contentMapper.countSendLogs(hotDealId);
    int appliedCount = contentMapper.countSendLogsByValidationOutcome(hotDealId, status);
    if (totalCount == 0 || appliedCount != totalCount) {
      throw new IllegalStateException(
          "이메일 검증 결과가 발송 로그 전체에 반영되지 않았습니다. hotDealId=%d, expectedValidation=%s, applied=%d, total=%d"
              .formatted(hotDealId, status, appliedCount, totalCount));
    }
    return appliedCount;
  }
}
