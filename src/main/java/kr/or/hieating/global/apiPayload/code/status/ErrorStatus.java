package kr.or.hieating.global.apiPayload.code.status;

import kr.or.hieating.global.apiPayload.code.BaseErrorCode;
import kr.or.hieating.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 공통 에러 코드 */
@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
  _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
  _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),

  // 프로모션(배너) 관련 에러
  PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROMOTION4001", "해당 배너 프로모션이 존재하지 않습니다."),
  PROMOTION_REORDER_LIMIT(
      HttpStatus.BAD_REQUEST, "PROMOTION4002", "더 이상 순서를 쪼갤 수 없습니다. 재배치가 필요합니다."),

  // 핫딜 관련 에러
  INVALID_SORT_BY(HttpStatus.BAD_REQUEST, "HOTDEAL4001", "유효하지 않은 정렬 기준입니다."),
  INVALID_START_DATE(HttpStatus.BAD_REQUEST, "HOTDEAL4002", "시작일은 오늘 이후로 설정해야 합니다."),
  INVALID_END_DATE(HttpStatus.BAD_REQUEST, "HOTDEAL4003", "종료일은 시작일 이후로 설정해야 합니다."),
  HOT_DEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "HOTDEAL4004", "해당 핫딜이 존재하지 않습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  @Override
  public ErrorReasonDTO getReason() {
    return ErrorReasonDTO.builder().message(message).code(code).isSuccess(false).build();
  }

  @Override
  public ErrorReasonDTO getReasonHttpStatus() {
    return ErrorReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(false)
        .httpStatus(httpStatus)
        .build();
  }
}
