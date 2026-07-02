package kr.or.hieating.promotion.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PromotionUpdateRequestDTO {

  @NotBlank(message = "프로모션 제목은 필수 입력값입니다.") private String title;

  private String link;

  @NotNull(message = "프로모션 시작일은 필수 입력값입니다.") @JsonFormat(pattern = "yyyy.MM.dd")
  private LocalDate startsAt;

  @NotNull(message = "프로모션 종료일은 필수 입력값입니다.") @JsonFormat(pattern = "yyyy.MM.dd")
  private LocalDate endsAt;

  @AssertTrue(message = "프로모션 시작일은 종료일보다 이전이거나 같아야 합니다.") public boolean isPeriodValid() {
    if (startsAt == null || endsAt == null) {
      return true;
    }
    return !startsAt.isAfter(endsAt);
  }
}
