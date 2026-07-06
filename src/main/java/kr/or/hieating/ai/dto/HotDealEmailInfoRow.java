package kr.or.hieating.ai.dto;

import java.time.LocalDateTime;

public record HotDealEmailInfoRow(
    Long hotDealId,
    String title,
    String description,
    LocalDateTime startsAt,
    LocalDateTime endsAt) {}
