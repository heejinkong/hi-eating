package kr.or.hieating.global.apiPayload.exception;

import kr.or.hieating.global.apiPayload.code.BaseErrorCode;
import kr.or.hieating.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 일반 예외 클래스 (에러 코드와 관련) */
@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

  private final BaseErrorCode code;

  public ErrorReasonDTO getErrorReason() {
    return this.code.getReason();
  }

  public ErrorReasonDTO getErrorReasonHttpStatus() {
    return this.code.getReasonHttpStatus();
  }
}
