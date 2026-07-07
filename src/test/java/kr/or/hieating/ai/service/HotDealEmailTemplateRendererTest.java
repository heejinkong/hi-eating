package kr.or.hieating.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class HotDealEmailTemplateRendererTest {

  @Test
  void rendersKoreanHtmlEmailWithBannerAndHotDealLink() {
    HotDealEmailTemplateRenderer renderer = new HotDealEmailTemplateRenderer();
    ReflectionTestUtils.setField(renderer, "hotDealBaseUrl", "https://greenfood.example.com/");
    ReflectionTestUtils.setField(renderer, "imageBaseUrl", "https://images.example.com/");

    HotDealEmailInfoRow hotDeal =
        new HotDealEmailInfoRow(
            38L,
            "[SOVS] 레몬 올리브유 핫딜",
            "건강한 식탁",
            LocalDateTime.of(2026, 7, 7, 0, 0),
            LocalDateTime.of(2026, 7, 10, 23, 59),
            "/images/lemon.jpg");
    List<HotDealEmailProductRow> products =
        List.of(new HotDealEmailProductRow("레몬 올리브유", "건강식품", 33000, 23100, 30));
    GeneratedHotDealEmailDto aiCopy =
        new GeneratedHotDealEmailDto(
            "무시되는 제목", "안녕하세요, 여러분! 이 Woche에 도착하는 {핫딜}입니다.\n\n합리적인 가격으로 만나보세요.");

    GeneratedHotDealEmailDto rendered = renderer.render(hotDeal, products, aiCopy);

    assertThat(rendered.subject()).isEqualTo("(광고) [하이이팅] 관심 상품 [레몬 올리브유] 핫딜 알림");
    assertThat(rendered.content())
        .contains("https://images.example.com/images/lemon.jpg")
        .contains("https://greenfood.example.com/hot-deals?sort=popular&amp;hotDealId=38")
        .contains("33,000원")
        .contains("23,100원")
        .contains("<p style=\"margin:0 0 16px;\">")
        .doesNotContain("Woche", "SOVS", "{", "}");
  }
}
