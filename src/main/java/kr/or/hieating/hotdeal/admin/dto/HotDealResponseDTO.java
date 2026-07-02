package kr.or.hieating.hotdeal.admin.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotDealResponseDTO {
  private Long id;
  private String title;
  private LocalDateTime startsAt;
  private LocalDateTime endsAt;
  private String status;
  private Integer productCount;
  private Integer discountRate;
}
