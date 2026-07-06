package kr.or.hieating.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.or.hieating.ai.dto.HotDealProductInfoDto;
import kr.or.hieating.ai.dto.HotDealTargetInfoDto;
import kr.or.hieating.ai.dto.UserProfileDto;
import kr.or.hieating.ai.dto.UserScoreDto;
import kr.or.hieating.ai.service.EmailGenerationAiService;
import kr.or.hieating.ai.service.EmailValidationAiService;
import kr.or.hieating.ai.service.TargetScorePolicy;
import kr.or.hieating.ai.service.TargetUserScoringAiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_INTEGRATION_TEST", matches = "true")
class RemoteOllamaIntegrationTest {

  @Autowired private EmailGenerationAiService generationAiService;

  @Autowired private EmailValidationAiService validationAiService;

  @Autowired private TargetUserScoringAiClient targetUserScoringAiClient;

  @Autowired private TargetScorePolicy targetScorePolicy;

  @Test
  void receivesGenerationAndValidationResponsesFromSeparateServers() {
    String generatedEmail = generationAiService.generate("건강식품 할인 안내 이메일을 한 문장으로 작성해 줘.");
    String validationResult =
        validationAiService.validate("다음 이메일을 검증하고 PASS 또는 FAIL로 시작해 줘: " + generatedEmail);

    assertThat(generatedEmail).isNotBlank();
    assertThat(validationResult).isNotBlank();
  }

  @Test
  void scoresHighActivityUserAboveLowActivityUser() {
    HotDealTargetInfoDto hotDeal =
        new HotDealTargetInfoDto(
            1L,
            "건강식품 할인",
            "비타민 할인 행사",
            List.of("건강식품"),
            List.of(new HotDealProductInfoDto("종합 비타민", 30000, 20000)));
    UserProfileDto highActivityUser =
        new UserProfileDto(
            1L,
            "OTHER",
            30,
            List.of("건강식품"),
            3,
            List.of("건강식품"),
            1,
            List.of("건강식품"),
            2,
            List.of("건강식품"),
            4.5);
    UserProfileDto lowActivityUser =
        new UserProfileDto(
            2L, "OTHER", 30, List.of(), 0, List.of(), 0, List.of("건강식품"), 1, List.of("건강식품"), null);

    UserScoreDto highAiScore =
        targetUserScoringAiClient.score(hotDeal, List.of(highActivityUser)).get(0);
    UserScoreDto lowAiScore =
        targetUserScoringAiClient.score(hotDeal, List.of(lowActivityUser)).get(0);
    UserScoreDto highScore = targetScorePolicy.normalize(hotDeal, highActivityUser, highAiScore);
    UserScoreDto lowScore = targetScorePolicy.normalize(hotDeal, lowActivityUser, lowAiScore);

    assertThat(highScore.userId()).isEqualTo(1L);
    assertThat(highScore.score()).isBetween(90, 100);
    assertThat(highScore.reason()).isNotBlank();
    assertThat(lowScore.userId()).isEqualTo(2L);
    assertThat(lowScore.score()).isBetween(0, 59);
    assertThat(lowScore.reason()).isNotBlank();
    assertThat(highScore.score()).isGreaterThan(lowScore.score());
  }
}
