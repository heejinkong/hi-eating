package kr.or.hieating.home.controller;

import kr.or.hieating.hotdeal.service.HotDealService;
import kr.or.hieating.product.service.ProductService;
import kr.or.hieating.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final PromotionService promotionService;
  private final HotDealService hotDealService;
  private final ProductService productService;

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("promotions", promotionService.findActivePromotions());
    model.addAttribute("hotDealProducts", hotDealService.findActiveHotDealProducts());
    model.addAttribute("mostPurchasedProducts", productService.findMostPurchasedProducts());
    model.addAttribute("contentTemplate", "home/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "home");
    model.addAttribute("pageScript", "home");
    return "layout/base";
  }
}
