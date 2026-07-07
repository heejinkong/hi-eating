package kr.or.hieating.chat.dto;

import java.util.List;

public record ChatRoomResponseDto(ChatRoomSummaryDto room, List<ChatMessageDto> messages) {}
