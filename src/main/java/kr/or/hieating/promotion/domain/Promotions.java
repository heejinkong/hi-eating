package kr.or.hieating.promotion.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotions {

  private int id;
  private String title;
  private String imgSrc;
  private String link;
  private int displayOrder;
  private LocalDateTime startsAt;
  private LocalDateTime endsAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
