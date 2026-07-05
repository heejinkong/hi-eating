package kr.or.hieating.ai;

import static org.assertj.core.api.Assertions.assertThat;

import kr.or.hieating.ai.service.EmailGenerationAiService;
import kr.or.hieating.ai.service.EmailValidationAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_INTEGRATION_TEST", matches = "true")
class RemoteOllamaIntegrationTest {

  @Autowired private EmailGenerationAiService generationAiService;

  @Autowired private EmailValidationAiService validationAiService;

  @Test
  void receivesGenerationAndValidationResponsesFromSeparateServers() {
    String generatedEmail = generationAiService.generate("건강식품 할인 안내 이메일을 한 문장으로 작성해 줘.");
    String validationResult =
        validationAiService.validate("다음 이메일을 검증하고 PASS 또는 FAIL로 시작해 줘: " + generatedEmail);

    assertThat(generatedEmail).isNotBlank();
    assertThat(validationResult).isNotBlank();
  }
}
