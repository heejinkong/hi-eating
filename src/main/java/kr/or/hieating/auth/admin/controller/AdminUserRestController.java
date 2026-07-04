package kr.or.hieating.auth.admin.controller;

import java.util.List;
import kr.or.hieating.auth.admin.dto.AdminUserRoleTargetDto;
import kr.or.hieating.auth.admin.service.AdminUserService;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/users")
public class AdminUserRestController {

  private final AdminUserService adminUserService;

  @GetMapping("/admin-candidates")
  public ApiResponse<List<AdminUserRoleTargetDto>> adminCandidates() {
    return ApiResponse.onSuccess(adminUserService.findAdminCandidates());
  }

  @GetMapping("/admins")
  public ApiResponse<List<AdminUserRoleTargetDto>> admins() {
    return ApiResponse.onSuccess(adminUserService.findRevocableAdmins());
  }

  @PostMapping("/{userId}/admin-role")
  public ApiResponse<Void> grantAdminRole(@PathVariable("userId") long userId) {
    try {
      adminUserService.grantAdminRole(userId);
      return ApiResponse.onSuccess(null);
    } catch (IllegalArgumentException exception) {
      return failure(exception.getMessage());
    }
  }

  @DeleteMapping("/{userId}/admin-role")
  public ApiResponse<Void> revokeAdminRole(@PathVariable("userId") long userId) {
    try {
      adminUserService.revokeAdminRole(userId);
      return ApiResponse.onSuccess(null);
    } catch (IllegalArgumentException exception) {
      return failure(exception.getMessage());
    }
  }

  private ApiResponse<Void> failure(String message) {
    return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(), message, null);
  }
}
