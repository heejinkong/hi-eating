package kr.or.hieating.utils;

import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResolver {

  private final AuthMapper authMapper;

  public Long currentUserIdOrNull() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return null;
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails userDetails) {
      Users user = authMapper.findByEmail(userDetails.getUsername());
      return user == null ? null : user.getId();
    }

    return null;
  }

  public Long requireCurrentUserId() {
    Long userId = currentUserIdOrNull();

    if (userId == null) {
      throw new AuthenticationCredentialsNotFoundException("인증된 사용자 정보를 찾을 수 없습니다.");
    }

    return userId;
  }

  public Long currentUserId() {
    return requireCurrentUserId();
  }
}
