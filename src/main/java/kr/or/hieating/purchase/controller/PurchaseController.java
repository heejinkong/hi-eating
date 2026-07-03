package kr.or.hieating.purchase.controller;

import kr.or.hieating.purchase.exception.PurchaseException;
import kr.or.hieating.purchase.service.PurchaseService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

  private final PurchaseService purchaseService;
  private final UserResolver userResolver;

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
