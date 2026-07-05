package kr.or.hieating.ai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import kr.or.hieating.ai.service.EmailGenerationAiService;
import kr.or.hieating.ai.service.EmailValidationAiService;
import org.junit.jupiter.api.Test;

class AiTestControllerTest {

  @Test
  void returnsResponsesFromBothOllamaServers() {
    EmailGenerationAiService generationService = mock(EmailGenerationAiService.class);
    EmailValidationAiService validationService = mock(EmailValidationAiService.class);
    when(generationService.generate(contains("이메일"))).thenReturn("생성된 이메일");
    when(validationService.validate(contains("생성된 이메일"))).thenReturn("PASS");
    AiTestController controller = new AiTestController(generationService, validationService);

    Map<String, String> response = controller.test();

    assertThat(response)
        .containsEntry("generatedEmail", "생성된 이메일")
        .containsEntry("validationResult", "PASS");
  }
}
