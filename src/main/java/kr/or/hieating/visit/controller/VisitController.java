package kr.or.hieating.visit.controller;

import kr.or.hieating.utils.UserResolver;
import kr.or.hieating.visit.dto.VisitProductListPageResponseDto;
import kr.or.hieating.visit.dto.VisitProductListSearchCondition;
import kr.or.hieating.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class VisitController {

  private final VisitService visitService;
  private final UserResolver userResolver;

  @GetMapping("/visits")
  public String visits(@RequestParam(defaultValue = "1") Integer page, Model model) {
    if (page == null || page < 1) {
      return "redirect:/visits?page=1";
    }

    VisitProductListSearchCondition condition =
        new VisitProductListSearchCondition(userResolver.requireCurrentUserId(), page);
    VisitProductListPageResponseDto productPage = visitService.findVisitProducts(condition);

    if (page > productPage.totalPages()) {
      return "redirect:/visits?page=" + productPage.totalPages();
    }

    model.addAttribute("contentTemplate", "visits/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "product-history");
    model.addAttribute("condition", condition);
    model.addAttribute("productPage", productPage);
    return "layout/base";
  }
}
