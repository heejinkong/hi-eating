package kr.or.hieating.ai.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotDealEmailPromptBuilder {

  private static final String INSTRUCTION =
      """
      다음 핫딜을 홍보하는 이메일 제목과 본문을 한국어로 작성하세요.

      작성 조건:
      - 같은 핫딜의 모든 발송 대상자가 함께 받는 공통 이메일입니다.
      - 제목은 80자 이내로 작성하고 핵심 상품과 할인 혜택을 명확히 표현하세요.
      - 본문에는 상품 특징, 할인 혜택, 판매 기간을 자연스럽게 포함하세요.
      - 입력에 없는 효능, 가격, 할인율, 재고, 무료 배송 정보를 만들어내지 마세요.
      - 과장 광고와 지나친 긴급성 표현을 피하세요.
      - 개인정보나 사용자 이름을 포함하지 마세요.
      - 반드시 {"subject":"제목","content":"본문"} 형태의 순수 JSON 객체만 반환하세요.

      [핫딜 데이터]
      %s
      """;

  private final ObjectMapper objectMapper;

  public String build(HotDealEmailInfoRow hotDeal, List<HotDealEmailProductRow> products) {
    try {
      String data =
          objectMapper.writeValueAsString(Map.of("hotDeal", hotDeal, "products", products));
      return INSTRUCTION.formatted(data);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("핫딜 이메일 생성 프롬프트 직렬화에 실패했습니다.", exception);
    }
  }
}
