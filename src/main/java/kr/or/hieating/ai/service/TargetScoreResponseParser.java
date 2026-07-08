package kr.or.hieating.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  public Map<Long, Integer> parseScoreMap(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new IllegalStateException("AI 대상 선정 응답이 비어 있습니다.");
    }
    try {
      JsonNode root = objectMapper.readTree(extractJson(rawResponse));
      if (!root.isObject()) {
        throw new IllegalStateException("AI 대상 선정 점수 응답이 JSON 객체가 아닙니다.");
      }
      Map<Long, Integer> scores = new LinkedHashMap<>();
      root.properties()
          .forEach(
              entry -> {
                try {
                  long userId = Long.parseLong(entry.getKey());
                  int score = entry.getValue().asInt(-1);
                  if (score < 0 || score > 100) {
                    throw new IllegalStateException("AI 대상 선정 점수가 범위를 벗어났습니다: " + score);
                  }
                  scores.put(userId, score);
                } catch (NumberFormatException exception) {
                  throw new IllegalStateException(
                      "AI 대상 선정 응답의 사용자 ID가 숫자가 아닙니다: " + entry.getKey(), exception);
                }
              });
      return scores;
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("AI 대상 선정 응답 JSON 파싱에 실패했습니다.", exception);
    }
  }

  public List<UserScoreDto> parse(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new IllegalStateException("AI 대상 선정 응답이 비어 있습니다.");
    }

    String cleaned = extractJson(rawResponse);
    try {
      JsonNode root = objectMapper.readTree(cleaned);
      JsonNode evaluations = root.isObject() ? root.get("evaluations") : root;
      if (evaluations == null || !evaluations.isArray()) {
        throw new IllegalStateException("AI 대상 선정 응답에 evaluations 배열이 없습니다.");
      }
      List<UserScoreDto> scores = new ArrayList<>();
      for (JsonNode node : evaluations) {
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

    if (objectStart >= 0 && (arrayStart < 0 || objectStart < arrayStart)) {
      int objectEnd = withoutFence.lastIndexOf('}');
      if (objectEnd >= objectStart) {
        return withoutFence.substring(objectStart, objectEnd + 1);
      }
    }

    if (arrayStart >= 0) {
      int arrayEnd = withoutFence.lastIndexOf(']');
      if (arrayEnd >= arrayStart) {
        return withoutFence.substring(arrayStart, arrayEnd + 1);
      }
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
