package kr.or.hieating.auth.controller;

import java.security.Principal;
import kr.or.hieating.auth.dto.EmailCheckResponseDto;
import kr.or.hieating.auth.dto.SignupRequestDto;
import kr.or.hieating.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/login")
  public String login(Principal principal, RedirectAttributes redirectAttributes, Model model) {
    setAuthPage(model, "auth/login");
    return "layout/base";
  }

  @GetMapping("/signup")
  public String signup(Model model) {
    setSignupPage(model);
    return "layout/base";
  }

  @GetMapping("/signup/email-check")
  @ResponseBody
  public EmailCheckResponseDto checkEmail(
      @RequestParam(name = "email", required = false) String email) {
    return authService.checkEmail(email);
  }

  @PostMapping("/signup")
  public String signup(
      SignupRequestDto request, Model model, RedirectAttributes redirectAttributes) {
    try {
      authService.signup(request);
    } catch (IllegalArgumentException exception) {
      model.addAttribute("signupError", exception.getMessage());
      model.addAttribute("signupRequest", request);
      setSignupPage(model);
      return "layout/base";
    }

    redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해 주세요.");
    return "redirect:/login";
  }

  private void setAuthPage(Model model, String contentTemplate) {
    model.addAttribute("contentTemplate", contentTemplate);
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "auth");
    // 로그인 페이지에 마스코트 JS 추가
    if ("auth/login".equals(contentTemplate)) {
      model.addAttribute("pageScript", "auth-login");
    }
  }

  private void setSignupPage(Model model) {
    setAuthPage(model, "auth/signup");
    model.addAttribute("pageScript", "auth-signup");
  }
}
