package kr.or.hieating.auth.validator;

import kr.or.hieating.auth.dto.SignupRequestDto;

public interface SignupValidator {
  void validate(SignupRequestDto request);
}
