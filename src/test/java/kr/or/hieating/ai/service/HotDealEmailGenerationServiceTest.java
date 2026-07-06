package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import kr.or.hieating.ai.mapper.HotDealEmailContentMapper;
import kr.or.hieating.ai.prompt.HotDealEmailPromptBuilder;
import org.junit.jupiter.api.Test;

class HotDealEmailGenerationServiceTest {

  private final HotDealEmailContentMapper contentMapper = mock(HotDealEmailContentMapper.class);
  private final HotDealEmailPromptBuilder promptBuilder = mock(HotDealEmailPromptBuilder.class);
  private final EmailGenerationAiService generationAiService = mock(EmailGenerationAiService.class);
  private final HotDealEmailResponseParser responseParser = mock(HotDealEmailResponseParser.class);
  private final HotDealEmailPersistenceService persistenceService =
      mock(HotDealEmailPersistenceService.class);
  private final HotDealEmailGenerationService service =
      new HotDealEmailGenerationService(
          contentMapper, promptBuilder, generationAiService, responseParser, persistenceService);

  @Test
  void generatesOneEmailAndAppliesItToSelectedUsers() {
    long hotDealId = 25L;
    HotDealEmailInfoRow hotDeal =
        new HotDealEmailInfoRow(
            hotDealId,
            "잡채 핫딜",
            "버섯이 듬뿍 들어간 잡채",
            LocalDateTime.of(2026, 7, 6, 0, 0),
            LocalDateTime.of(2026, 7, 8, 23, 59));
    List<HotDealEmailProductRow> products =
        List.of(new HotDealEmailProductRow("버섯 듬뿍 잡채", "간편식", 8500, 6800, 20));
    GeneratedHotDealEmailDto generated =
        new GeneratedHotDealEmailDto("잡채 20% 할인", "간편하게 즐기는 잡채를 만나보세요.");

    when(contentMapper.findHotDealInfo(hotDealId)).thenReturn(hotDeal);
    when(contentMapper.findHotDealProducts(hotDealId)).thenReturn(products);
    when(promptBuilder.build(hotDeal, products)).thenReturn("생성 프롬프트");
    when(generationAiService.generate("생성 프롬프트")).thenReturn("AI 원문");
    when(responseParser.parse("AI 원문")).thenReturn(generated);

    assertThat(service.generateAndSave(hotDealId)).isEqualTo(generated);

    verify(generationAiService).generate("생성 프롬프트");
    verify(persistenceService).saveAndApply(hotDealId, generated);
  }

  @Test
  void reusesExistingContentWithoutCallingAiAgain() {
    long hotDealId = 26L;
    GeneratedHotDealEmailDto existing = new GeneratedHotDealEmailDto("기존 제목", "기존 본문");
    when(contentMapper.findGeneratedContent(hotDealId)).thenReturn(existing);

    assertThat(service.generateAndSave(hotDealId)).isEqualTo(existing);

    verify(persistenceService).applyExisting(hotDealId, existing);
    verifyNoInteractions(promptBuilder, generationAiService, responseParser);
  }
}
