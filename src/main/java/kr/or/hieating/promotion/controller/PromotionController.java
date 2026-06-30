package kr.or.hieating.promotion.controller;

import kr.or.hieating.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;

  @GetMapping("/promotions")
  public String promotions(Model model) {
    model.addAttribute("promotions", promotionService.findActivePromotions());
    return "promotion/promotions";
  }
}
