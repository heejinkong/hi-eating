package kr.or.hieating.ai.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotDealEmailValidationPromptBuilder {

  private static final String INSTRUCTION =
      """
      아래 원본 핫딜 데이터와 생성된 홍보 이메일을 비교하여 품질을 엄격하게 검증하세요.

      검증 항목:
      - spellingValid: 한국어 맞춤법과 문장이 자연스러운가
      - contextValid: 이메일 문맥이 자연스럽고 핫딜 홍보 목적에 맞는가
      - productInfoValid: 상품명과 상품 정보가 원본 데이터와 일치하는가
      - discountRateValid: 가격과 할인율이 원본 데이터와 정확히 일치하는가
      - exaggerationFree: 입력에 없는 효능, 무료배송, 재고, 최저가 등 과장 표현이 없는가
      - lengthValid: 제목은 80자 이하이고 HTML 태그를 제외한 실제 본문은 100자 이상 2000자 이하인가

      이메일 본문은 HTML 템플릿입니다. HTML 태그, 링크, 템플릿 변수(예: {{고객명}}, {{핫딜상세링크}}), 그리고 고정 푸터 문구(예: "본 메일은 하이이팅의 핫딜 광고성 정보입니다", "판매 기간: ...")는 검증 대상에서 제외하고, AI가 작성한 본문 내용과 상품명·가격·할인율의 정확성만 검증하세요.

      하나라도 충족하지 못하면 해당 값은 false입니다.
      issues에는 실패한 항목의 구체적인 이유만 항목당 80자 이내로 작성하세요. 모두 통과하면 빈 배열을 작성하세요.
      반드시 아래 형태의 순수 JSON 객체만 반환하세요.
      {"spellingValid":true,"contextValid":true,"productInfoValid":true,"discountRateValid":true,"exaggerationFree":true,"lengthValid":true,"issues":[]}

      [검증 데이터]
      %s
      """;

  private final ObjectMapper objectMapper;

  public String build(
      HotDealEmailInfoRow hotDeal,
      List<HotDealEmailProductRow> products,
      GeneratedHotDealEmailDto email) {
    try {
      return INSTRUCTION.formatted(
          objectMapper.writeValueAsString(
              Map.of("hotDeal", hotDeal, "products", products, "email", email)));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("이메일 품질 검증 프롬프트 직렬화에 실패했습니다.", exception);
    }
  }
}
