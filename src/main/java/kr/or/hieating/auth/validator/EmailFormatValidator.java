package kr.or.hieating.auth.validator;

import java.util.regex.Pattern;
import kr.or.hieating.auth.dto.SignupRequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class EmailFormatValidator implements SignupValidator, EmailCheckValidator {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  @Override
  public void validate(SignupRequestDto request) {
    validate(request.getEmail());
  }

  @Override
  public void validate(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("이메일을 입력해 주세요.");
    }

    if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
      throw new IllegalArgumentException("이메일 형식을 확인해 주세요.");
    }
  }
}
