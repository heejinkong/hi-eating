package kr.or.hieating.email.publisher;

import java.time.LocalDateTime;

public record EmailPublishMessage(
    Long emailDraftId,
    Long hotDealId,
    Long userId,
    String recipientName,
    String recipientEmail,
    String hotDealTitle,
    String subject,
    String content,
    LocalDateTime requestedAt) {}
