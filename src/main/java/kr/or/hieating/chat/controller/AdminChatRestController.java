package kr.or.hieating.chat.controller;

import java.util.List;
import kr.or.hieating.chat.dto.ChatAdminStatusRequestDto;
import kr.or.hieating.chat.dto.ChatAdminStatusResponseDto;
import kr.or.hieating.chat.dto.ChatRoomResponseDto;
import kr.or.hieating.chat.dto.ChatRoomSummaryDto;
import kr.or.hieating.chat.service.ChatService;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/chats")
public class AdminChatRestController {

  private final ChatService chatService;
  private final UserResolver userResolver;

  @GetMapping("/rooms")
  public ApiResponse<List<ChatRoomSummaryDto>> rooms() {
    return ApiResponse.onSuccess(chatService.findRooms(userResolver.requireCurrentUserId()));
  }

  @GetMapping("/rooms/{roomId}/messages")
  public ApiResponse<ChatRoomResponseDto> messages(@PathVariable("roomId") long roomId) {
    return ApiResponse.onSuccess(
        chatService.getAdminRoomMessages(userResolver.requireCurrentUserId(), roomId));
  }

  @GetMapping("/status")
  public ApiResponse<ChatAdminStatusResponseDto> status() {
    String status = chatService.findAdminStatus(userResolver.requireCurrentUserId());
    return ApiResponse.onSuccess(new ChatAdminStatusResponseDto(status));
  }

  @PutMapping("/status")
  public ApiResponse<ChatAdminStatusResponseDto> updateStatus(
      @RequestBody ChatAdminStatusRequestDto request) {
    String status =
        chatService.updateAdminStatus(userResolver.requireCurrentUserId(), request.status());
    return ApiResponse.onSuccess(new ChatAdminStatusResponseDto(status));
  }
}
