package kr.or.hieating.hotdeal.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HotDeals {

  private int id;
  private int createdBy;
  private String title;
  private String description;
  private LocalDateTime startsAt;
  private LocalDateTime endsAt;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
