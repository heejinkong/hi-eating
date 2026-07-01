package kr.or.hieating.hotdeal.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HotDealCreateRequestDTO {
  @NotNull(message = "핫딜 제목은 필수 입력값입니다.") @Size(max = 10, message = "핫딜 제목은 공백 포함 10글자 이하로 입력해주세요.") private String title;

  @Size(max = 200, message = "핫딜 설명은 공백 포함 200글자 이하로 입력해주세요.") private String description;

  @NotNull(message = "핫딜 시작일은 필수 입력값입니다.") @JsonFormat(pattern = "yyyy.MM.dd")
  private LocalDate startsAt;

  @NotNull(message = "핫딜 종료일은 필수 입력값입니다.") @JsonFormat(pattern = "yyyy.MM.dd")
  private LocalDate endsAt;

  @NotNull(message = "핫딜 할인율은 필수 입력값입니다.") private Integer discountRate;

  @NotNull(message = "핫딜 상품 선택은 필수입니다.") private List<ProductItemDTO> products;

  @Getter
  @Builder
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @AllArgsConstructor
  public static class ProductItemDTO {
    private Integer productOptionId;
    private Integer originalPrice;
  }
}
