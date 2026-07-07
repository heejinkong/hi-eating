package kr.or.hieating.mypage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.security.HiEatingUserPrincipal;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.mypage.service.MyPageService;
import kr.or.hieating.purchase.dto.RecentPurchaseProductDto;
import kr.or.hieating.purchase.service.PurchaseService;
import kr.or.hieating.utils.UserResolver;
import kr.or.hieating.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MyPageController {

  private static final Logger log = LoggerFactory.getLogger(MyPageController.class);
  private final FavoriteService favoriteService;
  private final PurchaseService purchaseService;
  private final VisitService visitService;
  private final UserResolver userResolver;
  private final MyPageService myPageService;

  @GetMapping("/mypage")
  public String myPage(Model model) {
    Long userId = userResolver.requireCurrentUserId();

    int purchaseCount = purchaseService.countPurchases(userId);
    int favoriteCount = favoriteService.countFavorites(userId);
    int visitCount = visitService.countVisits(userId);
    RecentPurchaseProductDto recentPurchaseProduct =
        purchaseService.findLatestPurchaseProduct(userId).orElse(null);

    model.addAttribute("contentTemplate", "mypage/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "mypage");
    model.addAttribute("pageScript", "mypage");
    model.addAttribute(
        "summaryCards",
        List.of(
            Map.of("label", "주문 상품", "count", purchaseCount, "href", "/orders"),
            Map.of("label", "관심 상품", "count", favoriteCount, "href", "/favorites"),
            Map.of("label", "최근 본 상품", "count", visitCount, "href", "/visits")));
    model.addAttribute("recentPurchaseProduct", recentPurchaseProduct);
    return "layout/base";
  }

  @GetMapping("/mypage/edit")
  public String editMember(Model model) {
    Users member = myPageService.findCurrentMemberOrNull();
    if (member == null) {
      return "redirect:/login";
    }

    setEditPage(model, member);
    return "layout/base";
  }

  @PostMapping("/mypage/edit")
  public String updateMember(
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "birth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate birth,
      @RequestParam(name = "gender", required = false) String gender,
      Model model,
      RedirectAttributes redirectAttributes) {
    Users member = myPageService.findCurrentMemberOrNull();
    if (member == null) {
      return "redirect:/login";
    }

    try {
      String normalizedName = myPageService.updateCurrentMemberProfile(name, birth, gender);
      redirectAttributes.addFlashAttribute("editMessage", "회원 정보가 수정되었습니다.");
      refreshCurrentPrincipalName(normalizedName);
      return "redirect:/mypage/edit";
    } catch (IllegalArgumentException exception) {
      member.setName(name);
      member.setBirth(birth);
      member.setGender(gender);
      model.addAttribute("editError", exception.getMessage());
      setEditPage(model, member);
      return "layout/base";
    }
  }

  @PostMapping("/mypage/withdraw")
  public String withdrawMember(HttpServletRequest request) {
    myPageService.withdrawCurrentMember();

    SecurityContextHolder.clearContext();
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    return "redirect:/";
  }

  private void setEditPage(Model model, Users member) {
    model.addAttribute("contentTemplate", "member/edit");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "member-edit");
    model.addAttribute("pageScript", "member-edit");

    model.addAttribute("member", member);
  }

  private void refreshCurrentPrincipalName(String name) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof HiEatingUserPrincipal userPrincipal)) {
      return;
    }

    HiEatingUserPrincipal refreshedPrincipal =
        new HiEatingUserPrincipal(
            userPrincipal.getId(),
            userPrincipal.getEmail(),
            null,
            name,
            userPrincipal.getAuthorities());
    UsernamePasswordAuthenticationToken refreshedAuthentication =
        new UsernamePasswordAuthenticationToken(
            refreshedPrincipal, authentication.getCredentials(), authentication.getAuthorities());
    refreshedAuthentication.setDetails(authentication.getDetails());
    SecurityContextHolder.getContext().setAuthentication(refreshedAuthentication);
  }
}
