package kr.or.hieating.auth.validator;

import kr.or.hieating.auth.dto.SignupRequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RequiredSignupValidator implements SignupValidator {

  @Override
  public void validate(SignupRequestDto request) {
    if (request == null) {
      throw new IllegalArgumentException("회원가입 정보가 없습니다.");
    }

    requireText(request.getName(), "이름을 입력해 주세요.");
    requireText(request.getPassword(), "비밀번호를 입력해 주세요.");
    requireText(request.getEmail(), "이메일을 입력해 주세요.");
    requireText(request.getBirth(), "생년월일을 입력해 주세요.");
    requireText(request.getGender(), "성별을 선택해 주세요.");
  }

  private void requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
  }
}
