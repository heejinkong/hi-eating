package kr.or.hieating.hotdeal.controller;

import kr.or.hieating.hotdeal.service.HotDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HotDealController {

  private final HotDealService hotDealService;

  @GetMapping("/hot-deals")
  public String hotDeals(Model model) {
    model.addAttribute("hotDealProducts", hotDealService.findActiveHotDealProducts());
    model.addAttribute("contentTemplate", "hotdeal/hot-deals");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "hotdeal");
    model.addAttribute("pageScript", "hotdeal");
    return "layout/base";
  }
}
