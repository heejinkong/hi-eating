package kr.or.hieating.email.admin.controller;

import kr.or.hieating.email.admin.dto.AdminEmailDashboardDto;
import kr.or.hieating.email.admin.service.AdminEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/emails")
public class AdminEmailController {

  private final AdminEmailService adminEmailService;

  @GetMapping
  public String emails(@RequestParam(required = false) Long emailId, Model model) {
    AdminEmailDashboardDto dashboard = adminEmailService.getDashboard(emailId);

    model.addAttribute("dashboard", dashboard);
    model.addAttribute("emailDrafts", dashboard.getEmailDrafts());
    model.addAttribute("selectedEmailDraft", dashboard.getSelectedEmailDraft());
    model.addAttribute("emailSummary", dashboard.getSummary());
    model.addAttribute("contentTemplate", "admin/emails/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "admin-email");
    model.addAttribute("pageScript", "admin-email");
    return "layout/admin-base";
  }
}
