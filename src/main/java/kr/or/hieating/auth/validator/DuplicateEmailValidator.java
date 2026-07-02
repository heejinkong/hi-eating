package kr.or.hieating.auth.validator;

import kr.or.hieating.auth.dto.SignupRequestDto;
import kr.or.hieating.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@RequiredArgsConstructor
public class DuplicateEmailValidator implements SignupValidator, EmailCheckValidator {

  private final AuthMapper authMapper;

  @Override
  public void validate(SignupRequestDto request) {
    validate(request.getEmail());
  }

  @Override
  public void validate(String email) {
    if (authMapper.countByEmail(email) > 0) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }
  }
}
