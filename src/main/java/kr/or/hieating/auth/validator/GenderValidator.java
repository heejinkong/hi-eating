package kr.or.hieating.auth.validator;

import java.util.Set;
import kr.or.hieating.auth.dto.SignupRequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
public class GenderValidator implements SignupValidator {

  private static final Set<String> ALLOWED_GENDERS = Set.of("MALE", "FEMALE");

  @Override
  public void validate(SignupRequestDto request) {
    if (!ALLOWED_GENDERS.contains(request.getGender())) {
      throw new IllegalArgumentException("성별 값을 확인해 주세요.");
    }
  }
}
