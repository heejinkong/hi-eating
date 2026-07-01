package kr.or.hieating.global.apiPayload.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 에러 응답 DTO */
@Getter
@Builder
public class ErrorReasonDTO {
  private final HttpStatus httpStatus;
  private final boolean isSuccess;
  private final String code;
  private final String message;
}
