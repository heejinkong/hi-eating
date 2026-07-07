package kr.or.hieating.chat.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatRoomSummaryDto {
  private Long roomId;
  private Long userId;
  private String userName;
  private String userEmail;
  private Long assignedAdminId;
  private String assignedAdminName;
  private String assignedAdminStatus;
  private String status;
  private int userUnreadCount;
  private int adminUnreadCount;
  private LocalDateTime lastMessageAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String lastMessageContent;
  private String lastSenderType;
}
