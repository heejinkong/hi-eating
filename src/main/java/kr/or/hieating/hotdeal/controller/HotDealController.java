package kr.or.hieating.hotdeal.controller;

import java.util.List;
import kr.or.hieating.hotdeal.dto.ActiveHotDealResponseDto;
import kr.or.hieating.hotdeal.dto.HotDealProductListPageResponseDto;
import kr.or.hieating.hotdeal.dto.HotDealProductSearchCondition;
import kr.or.hieating.hotdeal.service.HotDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class HotDealController {

  private final HotDealService hotDealService;

  @GetMapping("/hot-deals")
  public String hotDeals(
      @RequestParam(required = false) Long hotDealId,
      @RequestParam(defaultValue = "popular") String sort,
      Model model) {
    HotDealProductSearchCondition condition =
        new HotDealProductSearchCondition(hotDealId, sort, 1, null);
    List<ActiveHotDealResponseDto> activeHotDeals = hotDealService.findActiveHotDeals();
    ActiveHotDealResponseDto selectedHotDeal = findSelectedHotDeal(activeHotDeals, hotDealId);

    model.addAttribute("activeHotDeals", activeHotDeals);
    model.addAttribute("selectedHotDeal", selectedHotDeal);
    model.addAttribute("selectedHotDealId", hotDealId);
    model.addAttribute("condition", condition);
    model.addAttribute("productPage", hotDealService.findHotDealProducts(condition));
    model.addAttribute("contentTemplate", "hotdeal/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "hotdeal");
    model.addAttribute("pageScript", "hotdeal");
    return "layout/base";
  }

  @GetMapping("/api/hot-deals/products")
  @ResponseBody
  public HotDealProductListPageResponseDto hotDealProducts(
      @RequestParam(required = false) Long hotDealId,
      @RequestParam(defaultValue = "popular") String sort,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(required = false) Integer size) {
    return hotDealService.findHotDealProducts(
        new HotDealProductSearchCondition(hotDealId, sort, page, size));
  }

  private ActiveHotDealResponseDto findSelectedHotDeal(
      List<ActiveHotDealResponseDto> activeHotDeals, Long hotDealId) {
    if (activeHotDeals.isEmpty()) {
      return null;
    }

    if (hotDealId == null) {
      return null;
    }

    return activeHotDeals.stream()
        .filter(hotDeal -> hotDeal.getId().equals(hotDealId))
        .findFirst()
        .orElse(activeHotDeals.get(0));
  }
}
