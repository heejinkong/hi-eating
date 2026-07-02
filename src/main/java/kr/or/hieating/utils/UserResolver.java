package kr.or.hieating.utils;

import org.springframework.stereotype.Component;

@Component
public class UserResolver {

  public Long currentUserId() {
    return 1L;

    // Spring Security 완성 시 사용
    //    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //
    //    if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser
    // user)) {
    //      throw new AuthenticationCredentialsNotFoundException("인증된 사용자 정보를 찾을 수 없습니다.");
    //    }
    //
    //    return user.userId();
  }
}
