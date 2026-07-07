package kr.or.hieating.chat.controller;

import kr.or.hieating.chat.dto.ChatRoomResponseDto;
import kr.or.hieating.chat.service.ChatService;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatRestController {

  private final ChatService chatService;
  private final UserResolver userResolver;

  @GetMapping("/api/chat/room")
  public ApiResponse<ChatRoomResponseDto> getCurrentUserRoom() {
    return ApiResponse.onSuccess(chatService.getUserRoom(userResolver.requireCurrentUserId()));
  }
}
