package kr.or.hieating.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageCreateCommand {
  private Long id;
  private Long roomId;
  private Long senderId;
  private String senderType;
  private String content;
}
