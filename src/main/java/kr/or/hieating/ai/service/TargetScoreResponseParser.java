package kr.or.hieating.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import kr.or.hieating.ai.dto.UserScoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TargetScoreResponseParser {

  private final ObjectMapper objectMapper;

  public List<UserScoreDto> parse(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new IllegalStateException("AI 대상 선정 응답이 비어 있습니다.");
    }

    String cleaned = extractJson(rawResponse);
    try {
      JsonNode root = objectMapper.readTree(cleaned);
      List<UserScoreDto> scores = new ArrayList<>();
      for (JsonNode node : root) {
        scores.add(
            new UserScoreDto(
                readLong(node.get("userId")),
                readInteger(node.get("score")),
                readReason(node.get("reason"))));
      }
      return scores;
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("AI 대상 선정 응답 JSON 파싱에 실패했습니다.", exception);
    }
  }

  private String extractJson(String response) {
    String withoutFence = response.replace("```json", "").replace("```", "").trim();
    int arrayStart = withoutFence.indexOf('[');
    int objectStart = withoutFence.indexOf('{');

    if (arrayStart >= 0 && (objectStart < 0 || arrayStart < objectStart)) {
      int arrayEnd = withoutFence.lastIndexOf(']');
      if (arrayEnd >= arrayStart) {
        return withoutFence.substring(arrayStart, arrayEnd + 1);
      }
    }

    if (objectStart >= 0) {
      int objectEnd = withoutFence.lastIndexOf('}');
      if (objectEnd < objectStart) {
        throw new IllegalStateException("AI 대상 선정 응답에 완성된 JSON 객체가 없습니다.");
      }
      return "[" + withoutFence.substring(objectStart, objectEnd + 1) + "]";
    }
    throw new IllegalStateException("AI 대상 선정 응답에 JSON 배열 또는 객체가 없습니다.");
  }

  private Long readLong(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    try {
      return Long.valueOf(node.asText().trim());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private Integer readInteger(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    try {
      return Integer.valueOf(node.asText().trim());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private String readReason(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (node.isArray()) {
      return StreamSupport.stream(node.spliterator(), false)
          .map(JsonNode::asText)
          .map(String::trim)
          .filter(value -> !value.isBlank())
          .collect(Collectors.joining(" "));
    }
    return node.asText().trim();
  }
}
