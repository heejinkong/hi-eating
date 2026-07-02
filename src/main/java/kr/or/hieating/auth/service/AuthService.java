package kr.or.hieating.auth.service;

import java.util.List;
import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.dto.EmailCheckResponseDto;
import kr.or.hieating.auth.dto.SignupRequestDto;
import kr.or.hieating.auth.dto.SignupUserParam;
import kr.or.hieating.auth.mapper.AuthMapper;
import kr.or.hieating.auth.validator.EmailCheckValidator;
import kr.or.hieating.auth.validator.SignupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final List<SignupValidator> signupValidators;
  private final List<EmailCheckValidator> emailCheckValidators;
  private final AuthMapper authMapper;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public EmailCheckResponseDto checkEmail(String email) {
    String normalizedEmail = email == null ? "" : email.trim();

    try {
      emailCheckValidators.forEach(validator -> validator.validate(normalizedEmail));
    } catch (IllegalArgumentException exception) {
      return EmailCheckResponseDto.failure(exception.getMessage());
    }

    return EmailCheckResponseDto.success();
  }

  @Transactional
  public void signup(SignupRequestDto request) {
    signupValidators.forEach(validator -> validator.validate(request)); // 연쇄책임패턴 회원가입 검증

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    SignupUserParam userParam = request.toUserParam(encodedPassword);

    authMapper.insertUser(userParam);
    Users createdUser = authMapper.findByEmail(userParam.getEmail());
    authMapper.insertUserAuth(createdUser.getId(), "ROLE_USER");
  }
}
