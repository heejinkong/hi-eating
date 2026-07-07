package kr.or.hieating.ai.service;

import java.util.List;
import kr.or.hieating.ai.dto.EmailQualityValidationResult;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import kr.or.hieating.ai.mapper.HotDealEmailContentMapper;
import kr.or.hieating.ai.prompt.HotDealEmailValidationPromptBuilder;
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
public class HotDealEmailQualityValidationService {

  private final HotDealEmailContentMapper contentMapper;
  private final HotDealEmailValidationPromptBuilder promptBuilder;
  private final EmailValidationAiService validationAiService;
  private final EmailQualityValidationResponseParser responseParser;
  private final HotDealEmailPersistenceService persistenceService;

  public EmailQualityValidationResult validateAndApply(
      long hotDealId, GeneratedHotDealEmailDto email) {
    HotDealEmailInfoRow hotDeal = contentMapper.findHotDealInfo(hotDealId);
    List<HotDealEmailProductRow> products = contentMapper.findHotDealProducts(hotDealId);
    if (hotDeal == null || products.isEmpty()) {
      throw new IllegalStateException("이메일 품질 검증용 핫딜 정보를 찾을 수 없습니다. hotDealId=" + hotDealId);
    }

    String prompt = promptBuilder.build(hotDeal, products, email);
    EmailQualityValidationResult result =
        responseParser.parse(validationAiService.validate(prompt));
    int updated = persistenceService.applyValidation(hotDealId, result);
    log.info(
        "[이메일 검증] 완료 hotDealId={} result={} 발송로그={}건",
        hotDealId,
        result.isPass() ? "PASS" : "FAIL",
        updated);
    return result;
  }
}
