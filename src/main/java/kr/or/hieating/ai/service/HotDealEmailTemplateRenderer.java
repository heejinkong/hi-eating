package kr.or.hieating.ai.service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

@Component
public class HotDealEmailTemplateRenderer {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
  private static final Pattern LATIN_WORD = Pattern.compile("(?i)[a-z]+(?:[-'][a-z]+)*");
  private static final Pattern BRACES = Pattern.compile("[{}]");

  @Value("${greenfood.email.hot-deal-base-url:http://localhost:8080}")
  private String hotDealBaseUrl;

  @Value("${greenfood.email.image-base-url:http://localhost}")
  private String imageBaseUrl;

  public boolean isHtmlEmail(String content) {
    return StringUtils.hasText(content)
        && content.stripLeading().toLowerCase(Locale.ROOT).startsWith("<!doctype html>");
  }

  public GeneratedHotDealEmailDto render(
      HotDealEmailInfoRow hotDeal,
      List<HotDealEmailProductRow> products,
      GeneratedHotDealEmailDto aiCopy) {
    String productName = koreanCopy(products.get(0).productName());
    String subject = "(광고) [하이이팅] 관심 상품 [%s] 핫딜 알림".formatted(abbreviate(productName, 42));
    String content =
        renderHtml(
            hotDeal,
            products,
            aiCopy.content(),
            hotDealUrl(hotDeal.hotDealId()),
            imageUrl(hotDeal.heroImageLocation()));
    return new GeneratedHotDealEmailDto(subject, content);
  }

  private String renderHtml(
      HotDealEmailInfoRow hotDeal,
      List<HotDealEmailProductRow> products,
      String aiContent,
      String hotDealUrl,
      String imageUrl) {
    String image =
        StringUtils.hasText(imageUrl)
            ? "<img src=\"%s\" alt=\"핫딜 대표 상품\" style=\"display:block;width:100%%;max-height:330px;object-fit:cover;border:0;\">"
                .formatted(escape(imageUrl))
            : "";

    return """
        <!doctype html>
        <html lang="ko">
        <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,'Apple SD Gothic Neo','Malgun Gothic',sans-serif;color:#222;">
          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#f4f4f4;padding:24px 0;">
            <tr><td align="center">
              <table role="presentation" width="640" cellspacing="0" cellpadding="0" border="0" style="width:100%%;max-width:640px;background:#fff;">
                <tr><td style="padding:32px 36px 18px;text-align:center;">
                  <div style="font-size:14px;font-weight:700;color:#ff4b2b;">오늘의 핫딜</div>
                  <h1 style="margin:10px 0 0;font-size:28px;line-height:1.35;">%s</h1>
                </td></tr>
                <tr><td>%s</td></tr>
                <tr><td style="padding:30px 36px 10px;font-size:16px;line-height:1.75;">%s</td></tr>
                <tr><td style="padding:10px 36px 24px;">%s</td></tr>
                <tr><td align="center" style="padding:8px 36px 36px;">
                  <a href="%s" style="display:inline-block;background:#ff4b2b;color:#fff;text-decoration:none;font-size:16px;font-weight:700;padding:15px 28px;border-radius:6px;">핫딜 바로 보기</a>
                </td></tr>
                <tr><td style="padding:20px 36px;background:#fafafa;color:#777;font-size:12px;line-height:1.6;">
                  본 메일은 하이이팅의 핫딜 광고성 정보입니다.<br>
                  판매 기간: %s부터 %s까지
                </td></tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """
        .formatted(
            escape(koreanCopy(hotDeal.title())),
            image,
            paragraphs(aiContent),
            productRows(products),
            escape(hotDealUrl),
            hotDeal.startsAt().format(DATE_FORMAT),
            hotDeal.endsAt().format(DATE_FORMAT));
  }

  private String productRows(List<HotDealEmailProductRow> products) {
    StringBuilder rows = new StringBuilder();
    for (HotDealEmailProductRow product : products) {
      rows.append(
          """
          <div style="border-top:1px solid #eee;padding:18px 0;">
            <div style="font-size:17px;font-weight:700;line-height:1.45;">%s</div>
            <div style="margin-top:8px;color:#999;text-decoration:line-through;">%s원</div>
            <div style="margin-top:3px;font-size:20px;font-weight:700;"><span style="color:#ff2f2f;">%d%% 할인</span>&nbsp; %s원</div>
          </div>
          """
              .formatted(
                  escape(koreanCopy(product.productName())),
                  price(product.originalPrice()),
                  product.discountRate(),
                  price(product.hotDealPrice())));
    }
    return rows.toString();
  }

  private String paragraphs(String value) {
    String[] blocks = koreanCopy(value).split("(?:\\r?\\n){2,}");
    StringBuilder html = new StringBuilder();
    for (String block : blocks) {
      if (StringUtils.hasText(block)) {
        html.append("<p style=\"margin:0 0 16px;\">")
            .append(escape(block.trim()).replace("\n", "<br>"))
            .append("</p>");
      }
    }
    return html.toString();
  }

  String koreanCopy(String value) {
    if (!StringUtils.hasText(value)) {
      return "";
    }
    return LATIN_WORD
        .matcher(BRACES.matcher(value).replaceAll(""))
        .replaceAll("")
        .replaceAll("\\[\\s*\\]", "")
        .replaceAll("[ \\t]+", " ")
        .replaceAll(" *\\n *", "\n")
        .trim();
  }

  private String hotDealUrl(long hotDealId) {
    return baseUrl() + "/hot-deals?sort=popular&hotDealId=" + hotDealId;
  }

  private String imageUrl(String imageLocation) {
    if (!StringUtils.hasText(imageLocation)
        || imageLocation.startsWith("http://")
        || imageLocation.startsWith("https://")) {
      return imageLocation;
    }
    return normalizedBaseUrl(imageBaseUrl)
        + (imageLocation.startsWith("/") ? imageLocation : "/" + imageLocation);
  }

  private String baseUrl() {
    return normalizedBaseUrl(hotDealBaseUrl);
  }

  private String normalizedBaseUrl(String value) {
    String baseUrl = value == null ? "" : value.trim();
    while (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    return baseUrl;
  }

  private String price(Integer value) {
    return NumberFormat.getIntegerInstance(Locale.KOREA).format(value == null ? 0 : value);
  }

  private String abbreviate(String value, int maxLength) {
    return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "…";
  }

  private String escape(String value) {
    return HtmlUtils.htmlEscape(value == null ? "" : value, "UTF-8");
  }
}
