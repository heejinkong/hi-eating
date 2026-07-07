package kr.or.hieating.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import kr.or.hieating.ai.dto.EmailQualityValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EmailQualityValidationResponseParser {

  private final ObjectMapper objectMapper;

  public EmailQualityValidationResult parse(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new IllegalArgumentException("AI 이메일 품질 검증 응답이 비어 있습니다.");
    }

    String json = extractJson(rawResponse);
    try {
      JsonNode root = objectMapper.readTree(json);
      List<String> issues = new ArrayList<>();
      root.path("issues").forEach(issue -> issues.add(issue.asText()));
      EmailQualityValidationResult result =
          new EmailQualityValidationResult(
              requiredBoolean(root, "spellingValid"),
              requiredBoolean(root, "contextValid"),
              requiredBoolean(root, "productInfoValid"),
              requiredBoolean(root, "discountRateValid"),
              requiredBoolean(root, "exaggerationFree"),
              requiredBoolean(root, "lengthValid"),
              issues);
      if (!result.isPass() && result.issues().isEmpty()) {
        throw new IllegalArgumentException("FAIL 검증 응답에는 issues가 하나 이상 필요합니다.");
      }
      return result;
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("AI 이메일 품질 검증 응답 JSON 파싱에 실패했습니다.", exception);
    }
  }

  private boolean requiredBoolean(JsonNode root, String field) {
    JsonNode value = root.get(field);
    if (value == null || !value.isBoolean()) {
      throw new IllegalArgumentException("AI 이메일 품질 검증 응답에 boolean 필드가 없습니다: " + field);
    }
    return value.booleanValue();
  }

  private String extractJson(String response) {
    String value =
        response.trim().replaceFirst("(?s)^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
    int start = value.indexOf('{');
    int end = value.lastIndexOf('}');
    if (start < 0 || end < start) {
      throw new IllegalArgumentException("AI 이메일 품질 검증 응답에서 JSON 객체를 찾을 수 없습니다.");
    }
    return value.substring(start, end + 1);
  }
}
