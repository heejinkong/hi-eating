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
      다음 핫딜 상품을 고객에게 추천하는 이유(추천사)를 한국어로 작성하세요.

      작성 조건:
      - 이메일 제목은 80자 이내의 친근하고 흥미를 끄는 한국어로 작성하세요.
      - 본문(content)에는 해당 핫딜 상품을 고객에게 왜 추천하는지, 고객 입장에서 어떤 매력이 있는지 설명하는 추천 사유를 2~3문장 이내로 자연스럽게 작성하세요.
      - 한국어 문장만 사용하세요. 영어, 일본어 등 외국어 단어나 불필요한 특수문자(예: 중괄호 {{ }})를 절대 사용하지 마세요.
      - 입력 상품명 앞에 영문 브랜드가 있더라도 영문 브랜드는 본문에 쓰지 마세요.
      - 입력에 없는 효능, 가격, 할인율, 재고, 무료 배송 정보를 지어내서 적지 마세요.
      - 과장 광고와 지나친 긴급성 표현을 피하세요.
      - 반드시 {"subject":"제목","content":"추천 사유"} 형태의 순수 JSON 객체만 반환하세요.
        - subject: 메일 제목
        - content: 핫딜 상품의 추천 사유 (2~3문장, 150자 이내)

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
