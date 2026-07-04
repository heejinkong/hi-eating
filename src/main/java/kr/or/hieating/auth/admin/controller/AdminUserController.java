package kr.or.hieating.auth.admin.controller;

import kr.or.hieating.auth.dto.SignupRequestDto;
import kr.or.hieating.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

  private final AuthService authService;

  @GetMapping
  public String adminUsers(Model model) {
    setAdminUsersPage(model);
    return "layout/admin-base";
  }

  @PostMapping
  public String createAdmin(
      SignupRequestDto request, Model model, RedirectAttributes redirectAttributes) {
    try {
      authService.createAdmin(request);
    } catch (IllegalArgumentException exception) {
      model.addAttribute("adminUserCreateError", exception.getMessage());
      model.addAttribute("signupRequest", request);
      setAdminUsersPage(model);
      return "layout/admin-base";
    }

    redirectAttributes.addFlashAttribute("adminUserCreateMessage", "관리자 계정이 발급되었습니다.");
    return "redirect:/admin/users";
  }

  private void setAdminUsersPage(Model model) {
    model.addAttribute("contentTemplate", "admin/users/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "admin-users");
    model.addAttribute("pageScript", "admin-users");
  }
}
