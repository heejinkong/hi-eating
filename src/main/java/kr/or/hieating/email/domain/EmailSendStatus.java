package kr.or.hieating.email.domain;

public enum EmailSendStatus {
  PENDING,
  NEEDS_REVIEW,
  APPROVED,
  PUBLISHED,
  SENDING,
  SENT,
  RETRYING,
  FAILED
}
