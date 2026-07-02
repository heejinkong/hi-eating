package kr.or.hieating.hotdeal.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActiveHotDealResponseDto {

  private Long id;
  private String title;
  private LocalDateTime startsAt;
  private LocalDateTime endsAt;
  private long remainingSeconds;
  private String heroImageLocation;
}
