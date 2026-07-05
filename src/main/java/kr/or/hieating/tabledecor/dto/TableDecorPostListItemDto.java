package kr.or.hieating.tabledecor.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableDecorPostListItemDto {

  private Long postId;
  private Long userId;
  private String userName;
  private Integer likeCount;
  private String imageSrc;
  private LocalDateTime createdAt;
  private boolean liked;
}
