package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import kr.or.hieating.ai.config.AiProperties;
import kr.or.hieating.ai.dto.HotDealInfoRow;
import kr.or.hieating.ai.dto.HotDealProductInfoDto;
import kr.or.hieating.ai.dto.TargetSelectionEvaluationDto;
import kr.or.hieating.ai.dto.TargetSelectionResult;
import kr.or.hieating.ai.dto.TargetUserDto;
import kr.or.hieating.ai.dto.UserProfileRow;
import kr.or.hieating.ai.dto.UserScoreDto;
import kr.or.hieating.ai.mapper.HotDealTargetMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
class TargetUserSelectionAiServiceTest {

  private final HotDealTargetMapper targetMapper = mock(HotDealTargetMapper.class);
  private final TargetUserScoringAiClient scoringClient = mock(TargetUserScoringAiClient.class);
  private final TargetScorePolicy scorePolicy = mock(TargetScorePolicy.class);
  private final TargetSelectionPersistenceService persistenceService =
      mock(TargetSelectionPersistenceService.class);
  private final TargetUserSelectionAiService service =
      new TargetUserSelectionAiService(
          targetMapper, scoringClient, scorePolicy, persistenceService, createProperties());

  {
    when(scorePolicy.normalize(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()))
        .thenAnswer(invocation -> invocation.getArgument(2));
  }

  @Test
  void selectsOnlyValidUsersAtOrAboveThreshold() {
    long hotDealId = 10L;
    when(targetMapper.findCategoryIdsByHotDealId(hotDealId)).thenReturn(List.of(3L));
    when(targetMapper.findCandidateUserIds(List.of(3L), 6)).thenReturn(List.of(1L, 2L, 3L));
    when(targetMapper.findHotDealInfo(hotDealId))
        .thenReturn(new HotDealInfoRow(hotDealId, "건강식품 핫딜", "설명", "건강식품"));
    when(targetMapper.findHotDealProducts(hotDealId))
        .thenReturn(List.of(new HotDealProductInfoDto("비타민", 10000, 7000)));
    when(targetMapper.findUserProfilesByIds(List.of(1L, 2L, 3L), List.of(3L), 6))
        .thenReturn(
            List.of(
                profile(1L, "one@example.com"),
                profile(2L, "two@example.com"),
                profile(3L, "three@example.com")));
    when(scoringClient.score(org.mockito.ArgumentMatchers.any(), anyList()))
        .thenReturn(
            List.of(
                new UserScoreDto(1L, 96, "구매 이력이 일치합니다."),
                new UserScoreDto(2L, 79, "관심도가 기준보다 낮습니다."),
                new UserScoreDto(999L, 100, "후보에 없는 사용자입니다."),
                new UserScoreDto(3L, 40, "관련 활동이 적습니다.")));
    when(persistenceService.save(eq(hotDealId), anyList(), anyList())).thenReturn(1);

    TargetSelectionResult result = service.selectAndSaveTargets(hotDealId);

    ArgumentCaptor<List<TargetSelectionEvaluationDto>> evaluationCaptor =
        ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<List<TargetUserDto>> targetCaptor = ArgumentCaptor.forClass(List.class);
    verify(persistenceService)
        .save(eq(hotDealId), evaluationCaptor.capture(), targetCaptor.capture());
    assertThat(evaluationCaptor.getValue())
        .containsExactly(
            new TargetSelectionEvaluationDto(1L, 96, "구매 이력이 일치합니다.", "SELECTED"),
            new TargetSelectionEvaluationDto(2L, 79, "관심도가 기준보다 낮습니다.", "REJECTED"),
            new TargetSelectionEvaluationDto(3L, 40, "관련 활동이 적습니다.", "REJECTED"));
    assertThat(targetCaptor.getValue())
        .containsExactly(new TargetUserDto(1L, "one@example.com", 96, "구매 이력이 일치합니다."));
    assertThat(result).isEqualTo(new TargetSelectionResult(hotDealId, 3, 1, 1));
  }

  @Test
  void skipsAiCallWhenNoCandidatesExist() {
    long hotDealId = 20L;
    when(targetMapper.findCategoryIdsByHotDealId(hotDealId)).thenReturn(List.of(3L));
    when(targetMapper.findCandidateUserIds(List.of(3L), 6)).thenReturn(List.of());

    TargetSelectionResult result = service.selectAndSaveTargets(hotDealId);

    assertThat(result).isEqualTo(TargetSelectionResult.empty(hotDealId));
    verify(scoringClient, never()).score(org.mockito.ArgumentMatchers.any(), anyList());
    verify(persistenceService, never()).save(eq(hotDealId), anyList(), anyList());
  }

  @Test
  void failsWholeJobWhenAiOmitsCandidate() {
    long hotDealId = 30L;
    when(targetMapper.findCategoryIdsByHotDealId(hotDealId)).thenReturn(List.of(3L));
    when(targetMapper.findCandidateUserIds(List.of(3L), 6)).thenReturn(List.of(1L, 2L));
    when(targetMapper.findHotDealInfo(hotDealId))
        .thenReturn(new HotDealInfoRow(hotDealId, "건강식품 핫딜", "설명", "건강식품"));
    when(targetMapper.findHotDealProducts(hotDealId))
        .thenReturn(List.of(new HotDealProductInfoDto("비타민", 10000, 7000)));
    when(targetMapper.findUserProfilesByIds(List.of(1L, 2L), List.of(3L), 6))
        .thenReturn(List.of(profile(1L, "one@example.com"), profile(2L, "two@example.com")));
    when(scoringClient.score(org.mockito.ArgumentMatchers.any(), anyList()))
        .thenReturn(List.of(new UserScoreDto(1L, 96, "구매 이력이 일치합니다.")));

    assertThatIllegalStateException()
        .isThrownBy(() -> service.selectAndSaveTargets(hotDealId))
        .withMessageContaining("AI 응답에서 사용자가 누락되었습니다");
    verify(persistenceService, never()).save(eq(hotDealId), anyList(), anyList());
  }

  private UserProfileRow profile(long userId, String email) {
    return new UserProfileRow(
        userId, email, "OTHER", LocalDate.of(1990, 1, 1), "건강식품", 2, "건강식품", 1, "건강식품", 1, 4.5);
  }

  private AiProperties createProperties() {
    AiProperties.Ollama ollama =
        new AiProperties.Ollama(
            "http://localhost:11434", "model", 0.2, Duration.ofSeconds(3), Duration.ofSeconds(120));
    return new AiProperties(ollama, ollama, new AiProperties.TargetSelection(80, 30, 6, 1, 3));
  }
}
