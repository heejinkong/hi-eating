package kr.or.hieating.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class HotDealEmailResponseParser {

  private static final int MAX_SUBJECT_LENGTH = 255;
  private static final Pattern SUBJECT_FIELD = Pattern.compile("(?i)[\\\"']?subject[\\\"']?\\s*:");
  private static final Pattern CONTENT_FIELD = Pattern.compile("(?i)[\\\"']?content[\\\"']?\\s*:");

  private final ObjectMapper objectMapper;

  public GeneratedHotDealEmailDto parse(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new IllegalStateException("AI 이메일 생성 응답이 비어 있습니다.");
    }

    int start = rawResponse.indexOf('{');
    int end = rawResponse.lastIndexOf('}');
    if (start < 0 || end < start) {
      throw new IllegalStateException("AI 이메일 생성 응답에 JSON 객체가 없습니다.");
    }

    String json = rawResponse.substring(start, end + 1);
    try {
      return toEmail(objectMapper.readTree(json));
    } catch (JsonProcessingException firstFailure) {
      String repaired = repairCommonJsonMistakes(json);
      try {
        return toEmail(objectMapper.readTree(repaired));
      } catch (JsonProcessingException secondFailure) {
        try {
          return parseByKnownFields(repaired);
        } catch (IllegalStateException fallbackFailure) {
          secondFailure.addSuppressed(firstFailure);
          fallbackFailure.addSuppressed(secondFailure);
          throw fallbackFailure;
        }
      }
    }
  }

  private String repairCommonJsonMistakes(String json) {
    return json.replaceAll("(?i)([,{]\\s*)(subject|content)\\s*:", "$1\"$2\":")
        .replaceAll("(\"\\s*)(\"(?:subject|content)\"\\s*:)", "$1,$2")
        .replaceAll(",\\s*}", "}");
  }

  private GeneratedHotDealEmailDto parseByKnownFields(String json) {
    Matcher subjectMatcher = SUBJECT_FIELD.matcher(json);
    Matcher contentMatcher = CONTENT_FIELD.matcher(json);
    if (!subjectMatcher.find()
        || !contentMatcher.find()
        || subjectMatcher.end() >= contentMatcher.start()) {
      throw new IllegalStateException("AI 이메일 생성 응답 JSON 파싱에 실패했습니다.");
    }

    String subject = cleanLooseValue(json.substring(subjectMatcher.end(), contentMatcher.start()));
    String content = cleanLooseValue(json.substring(contentMatcher.end(), json.lastIndexOf('}')));
    return validate(subject, content);
  }

  private String cleanLooseValue(String value) {
    String cleaned = value.trim();
    cleaned = cleaned.replaceFirst("^[,\\s]+", "").replaceFirst("[,\\s]+$", "");
    if (cleaned.length() >= 2
        && ((cleaned.startsWith("\"") && cleaned.endsWith("\""))
            || (cleaned.startsWith("'") && cleaned.endsWith("'")))) {
      cleaned = cleaned.substring(1, cleaned.length() - 1);
    }
    return cleaned.trim();
  }

  private GeneratedHotDealEmailDto toEmail(JsonNode root) {
    return validate(text(root.get("subject")), text(root.get("content")));
  }

  private GeneratedHotDealEmailDto validate(String subject, String content) {
    if (!StringUtils.hasText(subject) || !StringUtils.hasText(content)) {
      throw new IllegalStateException("AI 이메일 생성 응답의 제목 또는 본문이 비어 있습니다.");
    }
    if (subject.length() > MAX_SUBJECT_LENGTH) {
      throw new IllegalStateException("AI 이메일 제목이 255자를 초과했습니다.");
    }
    return new GeneratedHotDealEmailDto(subject, content);
  }

  private String text(JsonNode node) {
    return node == null || node.isNull() ? null : node.asText().trim();
  }
}
