package kr.or.hieating.chat.dto;

import java.util.List;

public record ChatMessagePageResponseDto(
    List<ChatMessageDto> messages, boolean hasMoreMessages, Long oldestMessageId) {}
