package kr.or.hieating.ai.service;

import java.util.List;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import kr.or.hieating.ai.mapper.HotDealEmailContentMapper;
import kr.or.hieating.ai.prompt.HotDealEmailPromptBuilder;
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
public class HotDealEmailGenerationService {

  private final HotDealEmailContentMapper contentMapper;
  private final HotDealEmailPromptBuilder promptBuilder;
  private final EmailGenerationAiService generationAiService;
  private final HotDealEmailResponseParser responseParser;
  private final HotDealEmailTemplateRenderer templateRenderer;
  private final HotDealEmailPersistenceService persistenceService;

  public GeneratedHotDealEmailDto generateAndSave(long hotDealId) {
    HotDealEmailInfoRow hotDeal = contentMapper.findHotDealInfo(hotDealId);
    if (hotDeal == null) {
      throw new IllegalStateException("이메일 생성용 핫딜 정보를 찾을 수 없습니다. hotDealId=" + hotDealId);
    }

    List<HotDealEmailProductRow> products = contentMapper.findHotDealProducts(hotDealId);
    if (products.isEmpty()) {
      throw new IllegalStateException("이메일 생성용 핫딜 상품을 찾을 수 없습니다. hotDealId=" + hotDealId);
    }

    GeneratedHotDealEmailDto existing = contentMapper.findGeneratedContent(hotDealId);
    if (existing != null) {
      GeneratedHotDealEmailDto reusable = existing;
      if (!templateRenderer.isHtmlEmail(existing.content())) {
        reusable = templateRenderer.render(hotDeal, products, existing);
        int updated = persistenceService.saveAndApply(hotDealId, reusable);
        log.info("[이메일 생성] 기존 일반 텍스트를 HTML로 전환 hotDealId={} 발송로그={}건", hotDealId, updated);
        return reusable;
      }
      int updated = persistenceService.applyExisting(hotDealId, reusable);
      log.info("[이메일 생성] 기존 콘텐츠 재사용 hotDealId={} 발송로그={}건", hotDealId, updated);
      return reusable;
    }

    String prompt = promptBuilder.build(hotDeal, products);
    GeneratedHotDealEmailDto aiCopy = responseParser.parse(generationAiService.generate(prompt));
    GeneratedHotDealEmailDto generated = templateRenderer.render(hotDeal, products, aiCopy);
    int updated = persistenceService.saveAndApply(hotDealId, generated);
    log.info("[이메일 생성] 완료 hotDealId={} 발송로그={}건", hotDealId, updated);
    return generated;
  }
}
