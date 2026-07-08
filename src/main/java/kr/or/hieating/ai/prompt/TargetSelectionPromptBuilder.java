package kr.or.hieating.ai.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import kr.or.hieating.ai.dto.HotDealTargetInfoDto;
import kr.or.hieating.ai.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetSelectionPromptBuilder {

  private static final String SYSTEM_PROMPT =
      """
      당신은 이메일 마케팅 대상 적합도 분석 AI입니다.
      핫딜과 사용자 활동 정보를 바탕으로 각 사용자의 구매 가능성을 0~100점으로 평가하세요.

      평가 기준:
      - 핫딜 카테고리와 구매/리뷰/즐겨찾기/관심 카테고리의 일치도
      - 최근 구매 및 리뷰 활동 횟수
      - 리뷰 평균 평점
      - 성별과 연령은 보조 정보로만 사용하고 차별적 판단을 하지 마세요.

      점수 구간:
      - 90~100점: 동일 카테고리 최근 구매 3회 이상이며 리뷰 또는 즐겨찾기 활동도 있음
      - 80~89점: 동일 카테고리 최근 구매 2회 이상이거나, 구매와 리뷰 및 즐겨찾기가 모두 있음
      - 60~79점: 동일 카테고리 최근 구매가 1회이거나 관련 활동이 보통 수준임
      - 0~59점: 동일 카테고리 구매 없이 즐겨찾기만 있거나 관련 활동이 매우 적음

      응답 규칙:
      - userId는 입력받은 값을 그대로 사용하세요.
      - score는 위 점수 구간을 적용한 0~100 사이의 정수여야 합니다.
      - 사용자의 활동 수치가 다르면 같은 점수를 기계적으로 반복하지 마세요.
      모든 입력 사용자에 대해 정확히 한 개의 결과를 반환하세요.
      응답은 사용자 ID를 키로, 점수를 값으로 갖는 순수 JSON 객체만 반환하세요.
      예: 입력 사용자 ID가 1, 2라면 {"1": 92, "2": 61}
      코드 블록이나 JSON 밖의 설명은 추가하지 마세요.
      사용자 ID 이외의 키를 추가하지 마세요.
      """;

  private final ObjectMapper objectMapper;

  public String systemPrompt() {
    return SYSTEM_PROMPT;
  }

  public String build(HotDealTargetInfoDto hotDeal, List<UserProfileDto> users) {
    try {
      return objectMapper.writeValueAsString(Map.of("hotDeal", hotDeal, "users", users));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("대상 선정 프롬프트 직렬화에 실패했습니다.", exception);
    }
  }
}
