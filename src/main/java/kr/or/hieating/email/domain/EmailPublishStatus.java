package kr.or.hieating.email.domain;

public enum EmailPublishStatus {
  PENDING,
  READY,
  PUBLISHING,
  PUBLISHED,
  SENDING,
  SENT,
  RETRYING,
  FAILED
}
