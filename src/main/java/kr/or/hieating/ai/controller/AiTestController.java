package kr.or.hieating.ai.controller;

import java.util.Map;
import kr.or.hieating.ai.service.EmailGenerationAiService;
import kr.or.hieating.ai.service.EmailValidationAiService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/test")
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "test-endpoint-enabled",
    havingValue = "true")
public class AiTestController {

  private final EmailGenerationAiService generationAiService;
  private final EmailValidationAiService validationAiService;

  public AiTestController(
      EmailGenerationAiService generationAiService, EmailValidationAiService validationAiService) {
    this.generationAiService = generationAiService;
    this.validationAiService = validationAiService;
  }

  @GetMapping
  public Map<String, String> test() {
    String generatedEmail =
        generationAiService.generate("고객에게 보낼 건강식품 할인 안내 이메일을 제목과 본문을 포함해 짧게 작성해 줘.");
    String validationResult =
        validationAiService.validate(
            """
            다음 이메일의 품질을 검증하고 PASS 또는 FAIL로 시작해 줘.

            %s
            """
                .formatted(generatedEmail));

    return Map.of("generatedEmail", generatedEmail, "validationResult", validationResult);
  }
}
