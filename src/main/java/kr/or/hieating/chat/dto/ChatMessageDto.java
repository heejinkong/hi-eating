package kr.or.hieating.chat.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatMessageDto {
  private Long id;
  private Long roomId;
  private Long senderId;
  private String senderType;
  private String senderName;
  private String content;
  private LocalDateTime createdAt;
}
