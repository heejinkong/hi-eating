package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.or.hieating.ai.dto.UserScoreDto;
import org.junit.jupiter.api.Test;

class TargetScoreResponseParserTest {

  private final TargetScoreResponseParser parser =
      new TargetScoreResponseParser(new ObjectMapper());

  @Test
  void parsesJsonCodeFenceResponse() {
    String response =
        """
        분석 결과입니다.
        ```json
        [{"userId":1,"score":96,"reason":"관심 카테고리가 일치합니다."}]
        ```
        """;

    List<UserScoreDto> scores = parser.parse(response);

    assertThat(scores).containsExactly(new UserScoreDto(1L, 96, "관심 카테고리가 일치합니다."));
  }

  @Test
  void parsesSingleJsonObjectResponse() {
    String response =
        """
        {"userId":2,"score":55,"reason":"즐겨찾기만 1회 있습니다."}
        """;

    List<UserScoreDto> scores = parser.parse(response);

    assertThat(scores).containsExactly(new UserScoreDto(2L, 55, "즐겨찾기만 1회 있습니다."));
  }

  @Test
  void parsesStringNumbersAndReasonArray() {
    String response =
        """
        [{"userId":"3","score":"82","reason":["구매 2회", "즐겨찾기 1회"]}]
        """;

    List<UserScoreDto> scores = parser.parse(response);

    assertThat(scores).containsExactly(new UserScoreDto(3L, 82, "구매 2회 즐겨찾기 1회"));
  }

  @Test
  void parsesSingleObjectContainingReasonArray() {
    String response =
        """
        분석 결과입니다.
        {"userId":4,"score":85,"reason":["구매 2회", "즐겨찾기 1회"]}
        """;

    List<UserScoreDto> scores = parser.parse(response);

    assertThat(scores).containsExactly(new UserScoreDto(4L, 85, "구매 2회 즐겨찾기 1회"));
  }

  @Test
  void rejectsResponseWithoutJson() {
    assertThatIllegalStateException()
        .isThrownBy(() -> parser.parse("응답을 생성하지 못했습니다."))
        .withMessage("AI 대상 선정 응답에 JSON 배열 또는 객체가 없습니다.");
  }
}
