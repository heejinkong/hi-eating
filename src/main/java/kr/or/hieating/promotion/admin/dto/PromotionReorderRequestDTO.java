package kr.or.hieating.promotion.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PromotionReorderRequestDTO {

  @NotNull private Integer movedPromotionId;

  private Integer previousPromotionId;

  private Integer nextPromotionId;

  @NotEmpty private List<Integer> orderedPromotionIds;
}
