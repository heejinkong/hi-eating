package kr.or.hieating.chat.service;

import java.util.List;
import java.util.Objects;
import kr.or.hieating.chat.dto.ChatMessageCreateCommand;
import kr.or.hieating.chat.dto.ChatMessageDto;
import kr.or.hieating.chat.dto.ChatMessagePageResponseDto;
import kr.or.hieating.chat.dto.ChatRoomCreateCommand;
import kr.or.hieating.chat.dto.ChatRoomResponseDto;
import kr.or.hieating.chat.dto.ChatRoomSummaryDto;
import kr.or.hieating.chat.dto.ChatWebSocketEvent;
import kr.or.hieating.chat.mapper.ChatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private static final int MAX_CONTENT_LENGTH = 1000;
  private static final String SENDER_TYPE_USER = "USER";
  private static final String SENDER_TYPE_ADMIN = "ADMIN";
  private static final String ADMIN_STATUS_ONLINE = "ONLINE";
  private static final String ADMIN_STATUS_AWAY = "AWAY";
  private static final String ADMIN_STATUS_OFFLINE = "OFFLINE";

  private final ChatMapper chatMapper;

  @Value("${chat.message-history-limit:50}")
  private int messageHistoryLimit;

  @Transactional
  public ChatRoomResponseDto getUserRoom(long userId) {
    ChatRoomSummaryDto room = findOrCreateRoom(userId);
    chatMapper.markUserRead(room.getRoomId());
    ChatRoomSummaryDto updatedRoom = requireRoom(room.getRoomId());
    ChatMessagePageResponseDto messagePage = findRecentMessages(updatedRoom.getRoomId());
    return new ChatRoomResponseDto(
        updatedRoom,
        messagePage.messages(),
        messagePage.hasMoreMessages(),
        messagePage.oldestMessageId());
  }

  @Transactional(readOnly = true)
  public List<ChatRoomSummaryDto> findRooms(long adminId) {
    return chatMapper.findRoomsByAdminId(adminId);
  }

  @Transactional
  public ChatRoomResponseDto getAdminRoomMessages(long adminId, long roomId) {
    ChatRoomSummaryDto room = requireAssignedRoom(adminId, roomId);
    chatMapper.markAdminRead(roomId);
    ChatRoomSummaryDto updatedRoom = requireRoom(roomId);
    ChatMessagePageResponseDto messagePage = findRecentMessages(room.getRoomId());
    return new ChatRoomResponseDto(
        updatedRoom,
        messagePage.messages(),
        messagePage.hasMoreMessages(),
        messagePage.oldestMessageId());
  }

  @Transactional(readOnly = true)
  public ChatMessagePageResponseDto findUserMessagesBefore(long userId, long beforeMessageId) {
    ChatRoomSummaryDto room =
        chatMapper
            .findRoomByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Chat room not found."));
    return findMessagesBefore(room.getRoomId(), beforeMessageId);
  }

  @Transactional(readOnly = true)
  public ChatMessagePageResponseDto findAdminMessagesBefore(
      long adminId, long roomId, long beforeMessageId) {
    requireAssignedRoom(adminId, roomId);
    return findMessagesBefore(roomId, beforeMessageId);
  }

  @Transactional
  public ChatWebSocketEvent sendUserMessage(long userId, String rawContent) {
    String content = normalizeContent(rawContent);
    ChatRoomSummaryDto room = findOrCreateRoom(userId);
    room = assignAdminIfNeeded(room);
    ChatMessageDto message = saveMessage(room.getRoomId(), userId, SENDER_TYPE_USER, content);
    chatMapper.updateRoomForUserMessage(room.getRoomId());
    if (room.getAssignedAdminId() != null) {
      chatMapper.recordAdminUserMessageReceived(room.getAssignedAdminId());
    }
    return ChatWebSocketEvent.message(requireRoom(room.getRoomId()), message);
  }

  @Transactional
  public ChatWebSocketEvent sendAdminMessage(long adminId, long roomId, String rawContent) {
    String content = normalizeContent(rawContent);
    requireAssignedRoom(adminId, roomId);
    ChatMessageDto message = saveMessage(roomId, adminId, SENDER_TYPE_ADMIN, content);
    int updated = chatMapper.updateRoomForAdminMessage(roomId, adminId);
    if (updated == 0) {
      throw new IllegalArgumentException("Chat room is not assigned to this admin.");
    }
    return ChatWebSocketEvent.message(requireRoom(roomId), message);
  }

  @Transactional
  public void markAdminConnected(long adminId) {
    chatMapper.markAdminConnected(adminId);
  }

  @Transactional
  public void markAdminDisconnected(long adminId) {
    chatMapper.markAdminDisconnected(adminId);
  }

  @Transactional(readOnly = true)
  public String findAdminStatus(long adminId) {
    return chatMapper.findAdminPresenceStatus(adminId).orElse(ADMIN_STATUS_OFFLINE);
  }

  @Transactional
  public String updateAdminStatus(long adminId, String status) {
    String normalizedStatus = normalizeAdminStatus(status);
    if (ADMIN_STATUS_OFFLINE.equals(normalizedStatus)) {
      throw new IllegalArgumentException("Admin status cannot be changed to OFFLINE manually.");
    }

    chatMapper.updateAdminPresenceStatus(adminId, normalizedStatus);
    return normalizedStatus;
  }

  private ChatRoomSummaryDto findOrCreateRoom(long userId) {
    return chatMapper
        .findRoomByUserId(userId)
        .orElseGet(
            () -> {
              ChatRoomCreateCommand command = new ChatRoomCreateCommand(null, userId);
              try {
                chatMapper.insertRoom(command);
              } catch (DuplicateKeyException ignored) {
                // 동시 요청으로 이미 방이 생성된 경우 기존 방을 반환
                return chatMapper
                    .findRoomByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Failed to find chat room."));
              }
              if (command.getId() == null) {
                throw new IllegalStateException("Failed to create chat room.");
              }
              return requireRoom(command.getId());
            });
  }

  private ChatRoomSummaryDto requireRoom(long roomId) {
    return chatMapper
        .findRoomById(roomId)
        .orElseThrow(() -> new IllegalArgumentException("Chat room not found."));
  }

  private ChatRoomSummaryDto requireAssignedRoom(long adminId, long roomId) {
    ChatRoomSummaryDto room = requireRoom(roomId);
    if (room.getAssignedAdminId() == null || !room.getAssignedAdminId().equals(adminId)) {
      throw new IllegalArgumentException("Chat room is not assigned to this admin.");
    }
    return room;
  }

  private ChatRoomSummaryDto assignAdminIfNeeded(ChatRoomSummaryDto room) {
    if (room.getAssignedAdminId() != null) {
      return room;
    }

    Long adminId =
        chatMapper
            .findAssignableAdminId(room.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("No admin is available for chat."));
    chatMapper.assignAdminToRoom(room.getRoomId(), adminId);
    return requireRoom(room.getRoomId());
  }

  private ChatMessageDto saveMessage(
      long roomId, long senderId, String senderType, String content) {
    ChatMessageCreateCommand command =
        new ChatMessageCreateCommand(null, roomId, senderId, senderType, content);
    chatMapper.insertMessage(command);
    if (command.getId() == null) {
      throw new IllegalStateException("Failed to create chat message.");
    }
    return chatMapper
        .findMessageById(command.getId())
        .orElseThrow(() -> new IllegalStateException("Failed to load chat message."));
  }

  private ChatMessagePageResponseDto findRecentMessages(long roomId) {
    int limit = normalizedMessageHistoryLimit();
    return toMessagePage(chatMapper.findRecentMessagesByRoomId(roomId, limit + 1), limit);
  }

  private ChatMessagePageResponseDto findMessagesBefore(long roomId, long beforeMessageId) {
    if (beforeMessageId <= 0) {
      throw new IllegalArgumentException("Message cursor is required.");
    }

    int limit = normalizedMessageHistoryLimit();
    return toMessagePage(
        chatMapper.findMessagesBeforeByRoomId(roomId, beforeMessageId, limit + 1), limit);
  }

  private ChatMessagePageResponseDto toMessagePage(
      List<ChatMessageDto> fetchedMessages, int limit) {
    boolean hasMoreMessages = fetchedMessages.size() > limit;
    List<ChatMessageDto> messages =
        hasMoreMessages
            ? fetchedMessages.subList(fetchedMessages.size() - limit, fetchedMessages.size())
            : fetchedMessages;
    Long oldestMessageId =
        messages.stream()
            .map(ChatMessageDto::getId)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    return new ChatMessagePageResponseDto(messages, hasMoreMessages, oldestMessageId);
  }

  private int normalizedMessageHistoryLimit() {
    return Math.max(1, messageHistoryLimit);
  }

  private String normalizeContent(String rawContent) {
    String content = rawContent == null ? "" : rawContent.trim();
    if (content.isEmpty()) {
      throw new IllegalArgumentException("Message content is required.");
    }
    if (content.length() > MAX_CONTENT_LENGTH) {
      throw new IllegalArgumentException("Message content is too long.");
    }
    return content;
  }

  private String normalizeAdminStatus(String rawStatus) {
    String status = rawStatus == null ? "" : rawStatus.trim().toUpperCase();
    if (!ADMIN_STATUS_ONLINE.equals(status)
        && !ADMIN_STATUS_AWAY.equals(status)
        && !ADMIN_STATUS_OFFLINE.equals(status)) {
      throw new IllegalArgumentException("Unsupported admin status.");
    }
    return status;
  }
}
