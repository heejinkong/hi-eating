package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import org.junit.jupiter.api.Test;

class HotDealEmailResponseParserTest {

  private final HotDealEmailResponseParser parser =
      new HotDealEmailResponseParser(new ObjectMapper());

  @Test
  void parsesJsonObjectInsideCodeFence() {
    GeneratedHotDealEmailDto email =
        parser.parse(
            """
            ```json
            {"subject":"잡채 20% 할인","content":"버섯 듬뿍 잡채를 만나보세요."}
            ```
            """);

    assertThat(email).isEqualTo(new GeneratedHotDealEmailDto("잡채 20% 할인", "버섯 듬뿍 잡채를 만나보세요."));
  }

  @Test
  void rejectsResponseWithoutContent() {
    assertThatIllegalStateException()
        .isThrownBy(() -> parser.parse("{\"subject\":\"제목만 있음\"}"))
        .withMessage("AI 이메일 생성 응답의 제목 또는 본문이 비어 있습니다.");
  }

  @Test
  void repairsUnquotedFieldNameAndTrailingComma() {
    GeneratedHotDealEmailDto email =
        parser.parse(
            """
            {"subject":"잡채 할인", content:"간편하게 즐겨보세요.",}
            """);

    assertThat(email).isEqualTo(new GeneratedHotDealEmailDto("잡채 할인", "간편하게 즐겨보세요."));
  }

  @Test
  void repairsMissingCommaBetweenSubjectAndContent() {
    GeneratedHotDealEmailDto email =
        parser.parse("{\"subject\":\"냉이 두부전 할인\"\"content\":\"간편하게 즐겨보세요.\"}");

    assertThat(email).isEqualTo(new GeneratedHotDealEmailDto("냉이 두부전 할인", "간편하게 즐겨보세요."));
  }

  @Test
  void extractsKnownFieldsWhenSubjectContainsUnescapedQuotes() {
    GeneratedHotDealEmailDto email =
        parser.parse("{\"subject\":\"오늘의 \"냉이 두부전\" 할인\",\"content\":\"향긋하게 즐겨보세요.\"}");

    assertThat(email).isEqualTo(new GeneratedHotDealEmailDto("오늘의 \"냉이 두부전\" 할인", "향긋하게 즐겨보세요."));
  }
}
