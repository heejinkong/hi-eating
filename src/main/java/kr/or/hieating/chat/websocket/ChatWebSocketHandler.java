package kr.or.hieating.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import kr.or.hieating.auth.security.HiEatingUserPrincipal;
import kr.or.hieating.chat.dto.ChatWebSocketEvent;
import kr.or.hieating.chat.dto.ChatWebSocketRequest;
import kr.or.hieating.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

  private static final String SESSION_USER_ATTRIBUTE = "chatSocketUser";
  private static final String SEND_MESSAGE_TYPE = "SEND_MESSAGE";
  private static final String ADMIN_MODE_QUERY = "mode=admin";

  private final ObjectMapper objectMapper;
  private final ChatService chatService;
  private final ConcurrentMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<Long, Set<WebSocketSession>> adminSessions =
      new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    ChatSocketUser socketUser = resolveSocketUser(session);
    if (socketUser == null) {
      session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication is required."));
      return;
    }

    session.getAttributes().put(SESSION_USER_ATTRIBUTE, socketUser);
    if (socketUser.adminMode()) {
      adminSessions
          .computeIfAbsent(socketUser.userId(), ignored -> ConcurrentHashMap.newKeySet())
          .add(session);
      chatService.markAdminConnected(socketUser.userId());
      return;
    }

    userSessions
        .computeIfAbsent(socketUser.userId(), ignored -> ConcurrentHashMap.newKeySet())
        .add(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    ChatSocketUser socketUser =
        (ChatSocketUser) session.getAttributes().get(SESSION_USER_ATTRIBUTE);
    if (socketUser == null) {
      sendToSession(session, ChatWebSocketEvent.error("Authentication is required."));
      return;
    }

    try {
      ChatWebSocketRequest request =
          objectMapper.readValue(message.getPayload(), ChatWebSocketRequest.class);
      if (!SEND_MESSAGE_TYPE.equals(request.type())) {
        sendToSession(session, ChatWebSocketEvent.error("Unsupported chat event type."));
        return;
      }

      ChatWebSocketEvent event;
      if (socketUser.adminMode()) {
        if (request.roomId() == null) {
          sendToSession(session, ChatWebSocketEvent.error("Chat room is required."));
          return;
        }
        event =
            chatService.sendAdminMessage(socketUser.userId(), request.roomId(), request.content());
      } else {
        event = chatService.sendUserMessage(socketUser.userId(), request.content());
      }

      broadcast(event);
    } catch (com.fasterxml.jackson.core.JsonProcessingException
        | IllegalArgumentException exception) {
      sendToSession(session, ChatWebSocketEvent.error(exception.getMessage()));
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    removeSession(session);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    removeSession(session);
  }

  private ChatSocketUser resolveSocketUser(WebSocketSession session) {
    Principal principal = session.getPrincipal();
    if (!(principal instanceof Authentication authentication)
        || !(authentication.getPrincipal() instanceof HiEatingUserPrincipal userPrincipal)) {
      return null;
    }

    boolean hasAdminRole =
        authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    boolean wantsAdminMode = wantsAdminMode(session.getUri());
    if (wantsAdminMode && !hasAdminRole) {
      return null;
    }

    return new ChatSocketUser(userPrincipal.getId(), wantsAdminMode);
  }

  private boolean wantsAdminMode(URI uri) {
    return uri != null && uri.getQuery() != null && uri.getQuery().contains(ADMIN_MODE_QUERY);
  }

  private void broadcast(ChatWebSocketEvent event) throws IOException {
    String payload = objectMapper.writeValueAsString(event);
    Long userId = event.room() == null ? null : event.room().getUserId();

    if (userId != null) {
      Set<WebSocketSession> sessions = userSessions.get(userId);
      if (sessions != null) {
        for (WebSocketSession session : sessions) {
          try {
            sendPayload(session, payload);
          } catch (IOException exception) {
            removeSession(session);
          }
        }
      }
    }

    Long adminId = event.room() == null ? null : event.room().getAssignedAdminId();
    if (adminId != null) {
      Set<WebSocketSession> sessions = adminSessions.get(adminId);
      if (sessions != null) {
        for (WebSocketSession session : sessions) {
          try {
            sendPayload(session, payload);
          } catch (IOException exception) {
            removeSession(session);
          }
        }
      }
    }
  }

  private void sendToSession(WebSocketSession session, ChatWebSocketEvent event)
      throws IOException {
    sendPayload(session, objectMapper.writeValueAsString(event));
  }

  private void sendPayload(WebSocketSession session, String payload) throws IOException {
    if (!session.isOpen()) {
      removeSession(session);
      return;
    }

    synchronized (session) {
      session.sendMessage(new TextMessage(payload));
    }
  }

  private void removeSession(WebSocketSession session) {
    Object value = session.getAttributes().get(SESSION_USER_ATTRIBUTE);
    if (!(value instanceof ChatSocketUser socketUser)) {
      return;
    }

    if (socketUser.adminMode()) {
      removeAdminSession(session, socketUser.userId());
      return;
    }

    Set<WebSocketSession> sessions = userSessions.get(socketUser.userId());
    if (sessions == null) {
      return;
    }

    sessions.remove(session);
    if (sessions.isEmpty()) {
      userSessions.remove(socketUser.userId(), sessions);
    }
  }

  private void removeAdminSession(WebSocketSession session, Long adminId) {
    Set<WebSocketSession> sessions = adminSessions.get(adminId);
    if (sessions == null) {
      return;
    }

    sessions.remove(session);
    if (!sessions.isEmpty()) {
      return;
    }

    adminSessions.remove(adminId, sessions);
    chatService.markAdminDisconnected(adminId);
  }

  private record ChatSocketUser(Long userId, boolean adminMode) {}
}
