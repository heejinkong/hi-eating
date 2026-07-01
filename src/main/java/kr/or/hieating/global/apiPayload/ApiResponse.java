package kr.or.hieating.global.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kr.or.hieating.global.apiPayload.code.BaseCode;
import kr.or.hieating.global.apiPayload.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 응답을 위한 공통 Response 클래스
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"}) // JSON 직렬화 시 필드 순서 지정
public class ApiResponse<T> {

  @JsonProperty("isSuccess")
  private final Boolean isSuccess;

  private final String code;
  private final String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final T result;

  // 성공 시 호출되는 래핑 메서드
  public static <T> ApiResponse<T> onSuccess(T result) {
    return new ApiResponse<>(
        true, SuccessStatus._OK.getCode(), SuccessStatus._OK.getMessage(), result);
  }

  // 성공 응답 생성 (커스텀 코드 및 메시지)
  public static <T> ApiResponse<T> of(BaseCode code, T result) {
    return new ApiResponse<>(
        true,
        code.getReasonHttpStatus().getCode(),
        code.getReasonHttpStatus().getMessage(),
        result);
  }

  // 실패 시 호출되는 래핑 메서드
  public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
    return new ApiResponse<>(false, code, message, data);
  }
}
