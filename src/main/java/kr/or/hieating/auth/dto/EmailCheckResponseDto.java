package kr.or.hieating.auth.dto;

public record EmailCheckResponseDto(boolean available, String message) {

  public static EmailCheckResponseDto success() {
    return new EmailCheckResponseDto(true, "사용 가능한 이메일입니다.");
  }

  public static EmailCheckResponseDto failure(String message) {
    return new EmailCheckResponseDto(false, message);
  }
}
