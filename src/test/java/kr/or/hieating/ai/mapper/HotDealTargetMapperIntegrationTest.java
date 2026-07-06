package kr.or.hieating.ai.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import kr.or.hieating.ai.dto.HotDealInfoRow;
import kr.or.hieating.ai.dto.TargetSelectionEvaluationDto;
import kr.or.hieating.ai.dto.TargetSelectionJobDto;
import kr.or.hieating.ai.dto.UserProfileRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(properties = "greenfood.ai.enabled=false")
@EnabledIfEnvironmentVariable(named = "DB_INTEGRATION_TEST", matches = "true")
class HotDealTargetMapperIntegrationTest {

  @Autowired private HotDealTargetMapper targetMapper;

  @Autowired private TargetSelectionJobMapper jobMapper;

  @Autowired private TargetSelectionHistoryMapper historyMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void queriesHotDealCandidatesAndProfilesFromOracle() {
    Long hotDealId =
        jdbcTemplate.queryForObject(
            "SELECT MAX(id) FROM hot_deals WHERE deleted_at IS NULL", Long.class);
    assumeTrue(hotDealId != null);

    List<Long> categoryIds = targetMapper.findCategoryIdsByHotDealId(hotDealId);
    HotDealInfoRow hotDeal = targetMapper.findHotDealInfo(hotDealId);

    assertThat(categoryIds).isNotEmpty();
    assertThat(hotDeal).isNotNull();
    assertThat(targetMapper.findHotDealProducts(hotDealId)).isNotEmpty();

    List<Long> candidateIds = targetMapper.findCandidateUserIds(categoryIds, 6);
    if (!candidateIds.isEmpty()) {
      List<UserProfileRow> profiles =
          targetMapper.findUserProfilesByIds(
              candidateIds.subList(0, Math.min(30, candidateIds.size())), categoryIds, 6);
      assertThat(profiles).isNotEmpty();
      assertThat(profiles).allSatisfy(profile -> assertThat(profile.email()).isNotBlank());
    }
  }

  @Test
  void persistsJobStateAndSelectionHistoryInOracle() {
    Long hotDealId =
        jdbcTemplate.queryForObject(
            "SELECT MAX(id) FROM hot_deals WHERE deleted_at IS NULL", Long.class);
    assumeTrue(hotDealId != null);

    jdbcTemplate.update("DELETE FROM ai_target_selection_jobs WHERE hot_deal_id = ?", hotDealId);
    jobMapper.insertPendingJob(hotDealId, 3);
    TargetSelectionJobDto job = jobMapper.findJobByHotDealId(hotDealId);
    assertThat(job.status()).isEqualTo("PENDING");

    assertThat(jobMapper.claimJob(job.id())).isEqualTo(1);
    jobMapper.markFailed(job.id(), "통합 테스트 오류");
    assertThat(jobMapper.findJobByHotDealId(hotDealId).status()).isEqualTo("RETRY_WAIT");

    List<Long> categoryIds = targetMapper.findCategoryIdsByHotDealId(hotDealId);
    List<Long> candidateIds = targetMapper.findCandidateUserIds(categoryIds, 6);
    assumeTrue(!candidateIds.isEmpty());
    Long userId = candidateIds.get(0);

    historyMapper.upsertEvaluations(
        hotDealId,
        List.of(new TargetSelectionEvaluationDto(userId, 95, "통합 테스트 선정 사유", "SELECTED")));

    Integer savedCount =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM hot_deal_target_selections
            WHERE hot_deal_id = ?
              AND user_id = ?
              AND ai_score = 95
              AND decision = 'SELECTED'
            """,
            Integer.class,
            hotDealId,
            userId);
    assertThat(savedCount).isEqualTo(1);
  }
}
