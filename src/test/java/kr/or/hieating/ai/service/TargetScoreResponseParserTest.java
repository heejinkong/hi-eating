package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class TargetScoreResponseParserTest {

  private final TargetScoreResponseParser parser =
      new TargetScoreResponseParser(new ObjectMapper());

  @Test
  void parsesCompactScoreMap() {
    var result = parser.parseScoreMap("{\"1\":92,\"2\":61}");

    assertThat(result).containsEntry(1L, 92).containsEntry(2L, 61);
  }

  @Test
  void parsesStructuredOllamaResponse() {
    var result =
        parser.parse(
            """
            {
              "evaluations": [
                {"userId": 1, "score": 92, "reason": "동일 카테고리 구매가 많음"},
                {"userId": 2, "score": 61, "reason": "관련 구매가 적음"}
              ]
            }
            """);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).userId()).isEqualTo(1L);
    assertThat(result.get(0).score()).isEqualTo(92);
  }

  @Test
  void keepsBackwardCompatibilityWithArrayResponse() {
    var result = parser.parse("[{\"userId\":1,\"score\":80,\"reason\":\"관련 활동 있음\"}]");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).score()).isEqualTo(80);
  }
}
