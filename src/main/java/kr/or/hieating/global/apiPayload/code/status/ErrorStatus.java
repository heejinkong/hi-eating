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
  INVALID_PROMOTION_DATE(HttpStatus.BAD_REQUEST, "PROMOTION4003", "프로모션 시작일은 종료일보다 이전이거나 같아야 합니다."),
  INVALID_FILE_TYPE(
      HttpStatus.BAD_REQUEST,
      "PROMOTION4004",
      "이미지 파일만 업로드할 수 있습니다. (허용 확장자: jpg, jpeg, png, gif, webp)"),
  EMPTY_FILE(HttpStatus.BAD_REQUEST, "PROMOTION4005", "업로드할 이미지 파일이 비어 있습니다."),

  // 리뷰 관련 에러
  REVIEW_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW4001", "리뷰를 작성할 수 있는 구매 내역이 없습니다."),
  DUPLICATE_REVIEW(HttpStatus.BAD_REQUEST, "REVIEW4002", "이미 작성된 리뷰가 있는 구매 내역입니다."),
  INVALID_REVIEW_TARGET(HttpStatus.BAD_REQUEST, "REVIEW4003", "구매 정보와 상품 정보가 일치하지 않습니다."),
  INVALID_REVIEW_IMAGE_FILE_TYPE(
      HttpStatus.BAD_REQUEST,
      "REVIEW4004",
      "이미지 파일만 업로드할 수 있습니다. (허용 확장자: jpg, jpeg, png, gif, webp)"),
  EMPTY_REVIEW_IMAGE_FILE(HttpStatus.BAD_REQUEST, "REVIEW4005", "업로드할 이미지 파일이 비어 있습니다."),
  REVIEW_IMAGE_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "REVIEW5001", "리뷰 이미지 업로드에 실패했습니다."),

  // 핫딜 관련 에러
  INVALID_SORT_BY(HttpStatus.BAD_REQUEST, "HOTDEAL4001", "유효하지 않은 정렬 기준입니다."),
  INVALID_START_DATE(HttpStatus.BAD_REQUEST, "HOTDEAL4002", "시작일은 오늘 이후로 설정해야 합니다."),
  INVALID_END_DATE(HttpStatus.BAD_REQUEST, "HOTDEAL4003", "종료일은 시작일 이후로 설정해야 합니다."),
  HOT_DEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "HOTDEAL4004", "해당 핫딜이 존재하지 않습니다."),
  INVALID_STATISTICS_DATE_RANGE(
      HttpStatus.BAD_REQUEST, "STATISTICS4001", "시작날짜는 끝날짜 이후로 설정할 수 없습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "회원 정보를 찾을 수 없습니다."),
  MEMBER_WITHDRAW_FAILED(HttpStatus.BAD_REQUEST, "MEMBER4002", "이미 탈퇴했거나 탈퇴할 수 없는 회원입니다."),

  // 구매 관련 에러
  PURCHASE_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "PURCHASE4001", "구매 수량은 1개 이상이어야 합니다."),
  PURCHASE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PURCHASE4002", "존재하지 않는 상품입니다."),
  PURCHASE_PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "PURCHASE4003", "현재 구매할 수 없는 상품입니다."),
  PURCHASE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PURCHASE5001", "구매 이력을 생성하지 못했습니다."),
  PURCHASE_OUT_OF_STOCK(HttpStatus.CONFLICT, "PURCHASE4004", "상품 재고가 부족합니다.");

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
