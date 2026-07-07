package kr.or.hieating.chat.dto;

public record ChatWebSocketRequest(String type, Long roomId, String content) {}
