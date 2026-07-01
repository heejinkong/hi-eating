package kr.or.hieating.global.apiPayload.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.global.apiPayload.code.ErrorReasonDTO;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** 예외 처리 클래스 (기본 표준 예외 가로채서 커스터마이징) */
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

  // @RequestParam 이나 @PathVariable 검증 에러 처리
  @ExceptionHandler
  public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
    String errorMessage =
        e.getConstraintViolations().stream()
            .map(constraintViolation -> constraintViolation.getMessage())
            .findFirst()
            .orElseThrow(
                () -> new RuntimeException("ConstraintViolationException 추출 중 에러가 발생했습니다."));

    try {
      ErrorStatus errorStatus = ErrorStatus.valueOf(errorMessage);
      return handleExceptionInternal(
          e, errorStatus.getCode(), errorStatus.getMessage(), errorStatus.getHttpStatus(), request);
    } catch (IllegalArgumentException iae) {
      return handleExceptionInternal(
          e,
          ErrorStatus._BAD_REQUEST.getCode(),
          errorMessage,
          ErrorStatus._BAD_REQUEST.getHttpStatus(),
          request);
    }
  }

  // @RequestBody DTO 검증 에러 처리
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError -> {
              String fieldName = fieldError.getField();
              String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
              errors.put(fieldName, errorMessage);
            });

    ApiResponse<Object> body =
        ApiResponse.onFailure(
            ErrorStatus._BAD_REQUEST.getCode(), ErrorStatus._BAD_REQUEST.getMessage(), errors);

    return super.handleExceptionInternal(e, body, headers, status, request);
  }

  // 예상하지 못한 에러 처리
  @ExceptionHandler
  public ResponseEntity<Object> exception(Exception e, WebRequest request) {
    log.error("Unhandled Exception: ", e);
    return handleExceptionInternal(
        e,
        ErrorStatus._INTERNAL_SERVER_ERROR.getCode(),
        ErrorStatus._INTERNAL_SERVER_ERROR.getMessage(),
        ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(),
        request);
  }

  // GeneralException 예외 발생시 처리 (개발자가 의도한 비즈니스 에러)
  @ExceptionHandler(value = GeneralException.class)
  public ResponseEntity<Object> onThrowException(
      GeneralException generalException, HttpServletRequest request) {
    ErrorReasonDTO errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();
    return handleExceptionInternal(
        generalException,
        errorReasonHttpStatus.getCode(),
        errorReasonHttpStatus.getMessage(),
        errorReasonHttpStatus.getHttpStatus(),
        new ServletWebRequest(request));
  }

  // 모든 내부 예외 조립에 사용되는 단 하나의 헬퍼 메서드
  private ResponseEntity<Object> handleExceptionInternal(
      Exception e, String code, String message, HttpStatusCode status, WebRequest request) {
    ApiResponse<Object> body = ApiResponse.onFailure(code, message, null);
    return super.handleExceptionInternal(e, body, HttpHeaders.EMPTY, status, request);
  }
}
