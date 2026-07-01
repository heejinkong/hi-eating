package kr.or.hieating.global.apiPayload.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** HTML 페이지 반환 컨트롤러(@Controller) 전용 예외 처리 클래스 */
@Slf4j
@ControllerAdvice(annotations = {Controller.class})
public class HtmlExceptionAdvice {

  // @RequestParam 이나 @PathVariable 검증 에러 처리
  @ExceptionHandler(ConstraintViolationException.class)
  public String validation(
      ConstraintViolationException e, HttpServletRequest request, Model model) {
    String errorMessage =
        e.getConstraintViolations().stream()
            .map(constraintViolation -> constraintViolation.getMessage())
            .findFirst()
            .orElse("검증 에러가 발생했습니다.");

    log.warn(
        "HTML Page Validation Exception occurred at [{}]: {}",
        request.getRequestURI(),
        errorMessage);

    try {
      ErrorStatus errorStatus = ErrorStatus.valueOf(errorMessage);
      model.addAttribute("errorCode", errorStatus.getCode());
      model.addAttribute("errorMessage", errorStatus.getMessage());
    } catch (IllegalArgumentException iae) {
      model.addAttribute("errorCode", ErrorStatus._BAD_REQUEST.getCode());
      model.addAttribute("errorMessage", errorMessage);
    }
    model.addAttribute("requestURI", request.getRequestURI());

    return "error/default";
  }

  // @RequestBody DTO 검증 에러 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public String handleMethodArgumentNotValid(
      MethodArgumentNotValidException e, HttpServletRequest request, Model model) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError -> {
              String fieldName = fieldError.getField();
              String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
              errors.put(fieldName, errorMessage);
            });

    log.warn(
        "HTML Page MethodArgumentNotValid Exception occurred at [{}]: {}",
        request.getRequestURI(),
        errors);

    model.addAttribute("errorCode", ErrorStatus._BAD_REQUEST.getCode());
    model.addAttribute("errorMessage", errors.values().stream().findFirst().orElse("잘못된 요청입니다."));
    model.addAttribute("requestURI", request.getRequestURI());

    return "error/default";
  }

  // 일반적인 예외(서버 오류 등) 발생 시 실행
  @ExceptionHandler(Exception.class)
  public String handleHtmlException(Exception e, HttpServletRequest request, Model model) {
    log.error("HTML Page Exception occurred at [{}]: ", request.getRequestURI(), e);

    model.addAttribute(
        "errorMessage", e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다.");
    model.addAttribute("requestURI", request.getRequestURI());

    return "error/default";
  }

  // 비즈니스 예외(GeneralException) 발생 시 실행
  @ExceptionHandler(GeneralException.class)
  public String handleHtmlGeneralException(
      GeneralException e, HttpServletRequest request, Model model) {
    log.warn(
        "HTML Page Business Exception occurred at [{}]: {}",
        request.getRequestURI(),
        e.getMessage());

    model.addAttribute("errorCode", e.getErrorReasonHttpStatus().getCode());
    model.addAttribute("errorMessage", e.getErrorReasonHttpStatus().getMessage());
    model.addAttribute("requestURI", request.getRequestURI());

    return "error/default";
  }
}
