package kr.or.hieating.auth.validator;

import kr.or.hieating.auth.dto.SignupRequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class PasswordPolicyValidator implements SignupValidator {

  @Override
  public void validate(SignupRequestDto request) {
    String password = request.getPassword();
    int categories = 0;

    if (password.chars().anyMatch(Character::isLowerCase)) {
      categories++;
    }
    if (password.chars().anyMatch(Character::isUpperCase)) {
      categories++;
    }
    if (password.chars().anyMatch(Character::isDigit)) {
      categories++;
    }
    if (password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
      categories++;
    }

    if (password.length() < 8 || categories < 3) {
      throw new IllegalArgumentException("비밀번호는 8자 이상, 영문 대소문자/숫자/특수문자 중 3종류 이상을 조합해 주세요.");
    }
  }
}
