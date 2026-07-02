package kr.or.hieating.promotion.admin.controller;

import java.util.List;
import kr.or.hieating.promotion.admin.service.AdminPromotionService;
import kr.or.hieating.promotion.domain.Promotions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminPromotionController {

  private final AdminPromotionService adminPromotionService;

  @GetMapping("/admin/promotions")
  public String getPromotionsPage(Model model) {
    List<Promotions> promotions = adminPromotionService.getAllPromotions();

    model.addAttribute("promotions", promotions);
    model.addAttribute("now", java.time.LocalDateTime.now());
    model.addAttribute("contentTemplate", "admin/promotions/settings");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "admin-promotion");
    model.addAttribute("pageScript", "admin-promotion");

    return "layout/admin-base";
  }
}
