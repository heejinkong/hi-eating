package kr.or.hieating.purchase.controller;

import kr.or.hieating.purchase.dto.PurchaseProductListPageResponseDto;
import kr.or.hieating.purchase.dto.PurchaseProductListSearchCondition;
import kr.or.hieating.purchase.exception.PurchaseException;
import kr.or.hieating.purchase.service.PurchaseService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

  private final PurchaseService purchaseService;
  private final UserResolver userResolver;

  @GetMapping("/orders")
  public String orders(@RequestParam(defaultValue = "1") Integer page, Model model) {
    if (page == null || page < 1) {
      return "redirect:/orders?page=1";
    }

    PurchaseProductListSearchCondition condition =
        new PurchaseProductListSearchCondition(userResolver.requireCurrentUserId(), page);
    PurchaseProductListPageResponseDto productPage =
        purchaseService.findPurchaseProducts(condition);

    if (page > productPage.totalPages()) {
      return "redirect:/orders?page=" + productPage.totalPages();
    }

    model.addAttribute("contentTemplate", "orders/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "product-history");
    model.addAttribute("condition", condition);
    model.addAttribute("productPage", productPage);
    return "layout/base";
  }

  @PostMapping("/product/{productId}/purchase")
  public String purchase(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "1") int quantity,
      RedirectAttributes redirectAttributes) {
    try {
      purchaseService.purchase(userResolver.requireCurrentUserId(), productId, quantity);
      redirectAttributes.addFlashAttribute("purchaseSuccessMessage", "구매가 완료되었습니다.");
    } catch (PurchaseException e) {
      redirectAttributes.addFlashAttribute("purchaseErrorMessage", e.getMessage());
    }

    return "redirect:/product/" + productId;
  }
}
