package kr.or.hieating.ai.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.or.hieating.ai.config.AiProperties;
import kr.or.hieating.ai.dto.HotDealInfoRow;
import kr.or.hieating.ai.dto.HotDealTargetInfoDto;
import kr.or.hieating.ai.dto.TargetSelectionEvaluationDto;
import kr.or.hieating.ai.dto.TargetSelectionResult;
import kr.or.hieating.ai.dto.TargetUserDto;
import kr.or.hieating.ai.dto.UserProfileDto;
import kr.or.hieating.ai.dto.UserProfileRow;
import kr.or.hieating.ai.dto.UserScoreDto;
import kr.or.hieating.ai.mapper.HotDealTargetMapper;
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
public class TargetUserSelectionAiService {

  private final HotDealTargetMapper hotDealTargetMapper;
  private final TargetUserScoringAiClient scoringClient;
  private final TargetScorePolicy scorePolicy;
  private final TargetSelectionPersistenceService persistenceService;
  private final AiProperties properties;

  public TargetSelectionResult selectAndSaveTargets(long hotDealId) {
    AiProperties.TargetSelection settings = properties.targetSelection();
    List<Long> categoryIds = hotDealTargetMapper.findCategoryIdsByHotDealId(hotDealId);
    if (categoryIds.isEmpty()) {
      log.warn("[대상선정] 핫딜 카테고리가 없습니다. hotDealId={}", hotDealId);
      return TargetSelectionResult.empty(hotDealId);
    }

    List<Long> candidateIds =
        hotDealTargetMapper.findCandidateUserIds(categoryIds, settings.recentMonths());
    if (candidateIds.isEmpty()) {
      log.info("[대상선정] 후보 사용자가 없습니다. hotDealId={}", hotDealId);
      return TargetSelectionResult.empty(hotDealId);
    }

    HotDealInfoRow hotDealRow = hotDealTargetMapper.findHotDealInfo(hotDealId);
    if (hotDealRow == null) {
      throw new IllegalStateException("대상 선정용 핫딜 정보를 찾을 수 없습니다. hotDealId=" + hotDealId);
    }
    HotDealTargetInfoDto hotDeal =
        HotDealTargetInfoDto.from(hotDealRow, hotDealTargetMapper.findHotDealProducts(hotDealId));

    Map<Long, TargetSelectionEvaluationDto> evaluationsByUserId = new LinkedHashMap<>();
    Map<Long, TargetUserDto> selectedByUserId = new LinkedHashMap<>();
    for (int start = 0; start < candidateIds.size(); start += settings.batchSize()) {
      int end = Math.min(start + settings.batchSize(), candidateIds.size());
      List<Long> batchIds = candidateIds.subList(start, end);
      selectBatch(hotDeal, categoryIds, batchIds, settings, evaluationsByUserId, selectedByUserId);
    }

    List<TargetSelectionEvaluationDto> evaluations = new ArrayList<>(evaluationsByUserId.values());
    List<TargetUserDto> targets = new ArrayList<>(selectedByUserId.values());
    int insertedCount = persistenceService.save(hotDealId, evaluations, targets);
    log.info(
        "[대상선정] 완료 hotDealId={} 후보={}명 선정={}명 저장={}명",
        hotDealId,
        candidateIds.size(),
        targets.size(),
        insertedCount);
    return new TargetSelectionResult(hotDealId, candidateIds.size(), targets.size(), insertedCount);
  }

  private void selectBatch(
      HotDealTargetInfoDto hotDeal,
      List<Long> categoryIds,
      List<Long> batchIds,
      AiProperties.TargetSelection settings,
      Map<Long, TargetSelectionEvaluationDto> evaluationsByUserId,
      Map<Long, TargetUserDto> selectedByUserId) {
    List<UserProfileRow> rows =
        hotDealTargetMapper.findUserProfilesByIds(batchIds, categoryIds, settings.recentMonths());
    if (rows.isEmpty()) {
      throw new IllegalStateException("후보 사용자 프로필을 조회하지 못했습니다. userIds=" + batchIds);
    }

    Map<Long, UserProfileRow> profilesById = new HashMap<>();
    Map<Long, UserProfileDto> profileDtosById = new HashMap<>();
    List<UserProfileDto> profiles = new ArrayList<>();
    for (UserProfileRow row : rows) {
      UserProfileDto profile = UserProfileDto.from(row);
      profilesById.put(row.userId(), row);
      profileDtosById.put(row.userId(), profile);
      profiles.add(profile);
    }
    Set<Long> missingProfiles = new HashSet<>(batchIds);
    missingProfiles.removeAll(profilesById.keySet());
    if (!missingProfiles.isEmpty()) {
      throw new IllegalStateException("후보 사용자 프로필을 조회하지 못했습니다. userIds=" + missingProfiles);
    }

    Set<Long> expectedIds = new HashSet<>(profilesById.keySet());
    List<UserScoreDto> scores = scoringClient.score(hotDeal, profiles);

    Set<Long> respondedIds = new HashSet<>();
    for (UserScoreDto score : scores) {
      if (!isValidScore(score, expectedIds) || !respondedIds.add(score.userId())) {
        log.warn("[대상선정] 유효하지 않은 AI 평가를 무시합니다. score={}", score);
        continue;
      }

      UserProfileRow profile = profilesById.get(score.userId());
      UserScoreDto normalizedScore =
          scorePolicy.normalize(hotDeal, profileDtosById.get(score.userId()), score);
      boolean selected = normalizedScore.score() >= settings.scoreThreshold();
      evaluationsByUserId.putIfAbsent(
          score.userId(),
          new TargetSelectionEvaluationDto(
              score.userId(),
              normalizedScore.score(),
              normalizedScore.reason(),
              selected ? "SELECTED" : "REJECTED"));
      if (!selected) {
        continue;
      }
      selectedByUserId.putIfAbsent(
          score.userId(),
          new TargetUserDto(
              score.userId(), profile.email(), normalizedScore.score(), normalizedScore.reason()));
    }

    Set<Long> missingIds = new HashSet<>(expectedIds);
    missingIds.removeAll(respondedIds);
    if (!missingIds.isEmpty()) {
      throw new IllegalStateException("AI 응답에서 사용자가 누락되었습니다. userIds=" + missingIds);
    }
  }

  private boolean isValidScore(UserScoreDto score, Set<Long> expectedIds) {
    return score != null
        && score.userId() != null
        && expectedIds.contains(score.userId()) // 후보에 없는 사용자 ID를 AI가 만들어내면 무시
        && score.score() != null
        && score.score() >= 0
        && score.score() <= 100
        && score.reason() != null
        && !score.reason().isBlank(); // 선정 사유가 없으면 무시
  }
}
