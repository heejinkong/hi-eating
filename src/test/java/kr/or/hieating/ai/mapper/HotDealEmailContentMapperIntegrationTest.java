package kr.or.hieating.ai.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(properties = "greenfood.ai.enabled=false")
@EnabledIfEnvironmentVariable(named = "DB_INTEGRATION_TEST", matches = "true")
class HotDealEmailContentMapperIntegrationTest {

  @Autowired private HotDealEmailContentMapper contentMapper;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void readsHotDealAndPersistsOneSharedEmailContent() {
    Long hotDealId =
        jdbcTemplate.queryForObject(
            "SELECT MAX(id) FROM hot_deals WHERE deleted_at IS NULL", Long.class);
    assumeTrue(hotDealId != null);
    assumeTrue(!contentMapper.findHotDealProducts(hotDealId).isEmpty());

    assertThat(contentMapper.findHotDealInfo(hotDealId)).isNotNull();
    contentMapper.upsertGeneratedContent(hotDealId, "통합 테스트 제목", "통합 테스트 본문");
    assertThat(contentMapper.findGeneratedContent(hotDealId))
        .isEqualTo(new GeneratedHotDealEmailDto("통합 테스트 제목", "통합 테스트 본문"));
  }
}
