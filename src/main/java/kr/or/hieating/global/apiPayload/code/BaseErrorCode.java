package kr.or.hieating.global.apiPayload.code;

/** 공통 에러 코드 인터페이스 */
public interface BaseErrorCode {
  ErrorReasonDTO getReason();

  ErrorReasonDTO getReasonHttpStatus();
}
