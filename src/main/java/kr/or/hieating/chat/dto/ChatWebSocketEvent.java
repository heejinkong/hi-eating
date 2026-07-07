package kr.or.hieating.chat.dto;

public record ChatWebSocketEvent(
    String type, ChatRoomSummaryDto room, ChatMessageDto message, String error) {

  public static ChatWebSocketEvent message(ChatRoomSummaryDto room, ChatMessageDto message) {
    return new ChatWebSocketEvent("MESSAGE", room, message, null);
  }

  public static ChatWebSocketEvent error(String error) {
    return new ChatWebSocketEvent("ERROR", null, null, error);
  }
}
