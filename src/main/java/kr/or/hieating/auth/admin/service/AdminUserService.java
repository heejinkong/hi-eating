package kr.or.hieating.auth.admin.service;

import java.util.List;
import kr.or.hieating.auth.admin.dto.AdminUserPageResponseDto;
import kr.or.hieating.auth.admin.dto.AdminUserRoleTargetDto;
import kr.or.hieating.auth.mapper.AuthMapper;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private static final String ROLE_ADMIN = "ROLE_ADMIN";
  private static final String ROLE_USER = "ROLE_USER";

  private final AuthMapper authMapper;
  private final UserResolver userResolver;

  @Transactional(readOnly = true)
  public List<AdminUserRoleTargetDto> findAdminCandidates() {
    return authMapper.findUsersWithoutAdminRole();
  }

  @Transactional(readOnly = true)
  public AdminUserPageResponseDto findAdminCandidatesByPage(String keyword, int page, int size) {
    if (page < 1) {
      throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
    }
    if (size < 1) {
      throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
    }

    int totalCount = authMapper.countAdminCandidates(keyword);
    int totalPages = (int) Math.ceil((double) totalCount / size);
    int offset = (page - 1) * size;
    List<AdminUserRoleTargetDto> users =
        authMapper.findAdminCandidatesByPage(keyword, offset, size);
    return new AdminUserPageResponseDto(users, page, size, totalCount, totalPages);
  }

  @Transactional(readOnly = true)
  public List<AdminUserRoleTargetDto> findRevocableAdmins() {
    return authMapper.findAdminUsersExcluding(userResolver.requireCurrentUserId());
  }

  @Transactional
  public void grantAdminRole(long userId) {
    validateExistingUser(userId);

    if (hasRole(userId, ROLE_ADMIN)) {
      throw new IllegalArgumentException("이미 관리자 권한이 있는 사용자입니다.");
    }

    authMapper.insertUserAuth(userId, ROLE_ADMIN);
  }

  @Transactional
  public void revokeAdminRole(long userId) {
    Long currentUserId = userResolver.requireCurrentUserId();
    if (currentUserId == userId) {
      throw new IllegalArgumentException("본인의 관리자 권한은 회수할 수 없습니다.");
    }

    validateExistingUser(userId);
    if (!hasRole(userId, ROLE_ADMIN)) {
      throw new IllegalArgumentException("관리자 권한이 없는 사용자입니다.");
    }

    ensureUserRole(userId);

    int deleted = authMapper.deleteUserAuth(userId, ROLE_ADMIN);
    if (deleted == 0) {
      throw new IllegalArgumentException("관리자 권한이 없는 사용자입니다.");
    }
  }

  private void validateExistingUser(long userId) {
    if (authMapper.countUserById(userId) == 0) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }
  }

  private void ensureUserRole(long userId) {
    if (!hasRole(userId, ROLE_USER)) {
      authMapper.insertUserAuth(userId, ROLE_USER);
    }
  }

  private boolean hasRole(long userId, String role) {
    return authMapper.countUserAuthority(userId, role) > 0;
  }
}
