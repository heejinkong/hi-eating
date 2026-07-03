package kr.or.hieating.product.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import kr.or.hieating.review.domain.ReviewSummary;
import kr.or.hieating.utils.HtmlSanitizer;

public record ProductDetail(
    Long id,
    Long categoryId,
    String categoryName,
    String name,
    String description,
    int price,
    Integer salePrice,
    int discountRate,
    int viewCount,
    String status,
    LocalDateTime createdAt,
    List<String> imageUrls,
    List<ProductOption> options,
    ReviewSummary reviewSummary,
    boolean favorite) {

  public String formattedPrice() {
    return String.format(Locale.KOREA, "%,d원", price);
  }

  public boolean hasHotDeal() {
    return salePrice != null && salePrice > 0 && salePrice < price;
  }

  public String formattedSalePrice() {
    return String.format(Locale.KOREA, "%,d원", hasHotDeal() ? salePrice : price);
  }

  public String safeDescriptionHtml() {
    return HtmlSanitizer.sanitize(description);
  }

  public String mainImageUrl() {
    return imageUrls.get(0);
  }

  public List<String> thumbnailImageUrls() {
    return imageUrls;
  }

  public String statusLabel() {
    return switch (status) {
      case "ON_SALE" -> "판매중";
      case "SOLD_OUT" -> "품절";
      case "STOPPED" -> "판매중지";
      default -> status;
    };
  }

  public int totalStock() {
    return options.stream().mapToInt(ProductOption::stock).sum();
  }
}
