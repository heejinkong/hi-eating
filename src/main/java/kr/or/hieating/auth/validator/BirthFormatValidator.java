package kr.or.hieating.auth.validator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import kr.or.hieating.auth.dto.SignupRequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class BirthFormatValidator implements SignupValidator {

  @Override
  public void validate(SignupRequestDto request) {
    LocalDate birthDate;
    try {
      birthDate = request.getBirthDate();
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException("생년월일은 20010415 형식으로 입력해 주세요.");
    }

    if (birthDate.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("생년월일은 오늘 이후 날짜를 입력할 수 없습니다.");
    }
  }
}
