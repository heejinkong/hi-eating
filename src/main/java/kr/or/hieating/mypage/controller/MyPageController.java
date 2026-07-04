package kr.or.hieating.mypage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.or.hieating.auth.domain.Users;
import kr.or.hieating.auth.mapper.AuthMapper;
import kr.or.hieating.auth.security.HiEatingUserPrincipal;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.product.domain.Product;
import kr.or.hieating.purchase.dto.RecentPurchaseProductDto;
import kr.or.hieating.purchase.service.PurchaseService;
import kr.or.hieating.user.domain.User;
import kr.or.hieating.utils.UserResolver;
import kr.or.hieating.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
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

  private final FavoriteService favoriteService;
  private final PurchaseService purchaseService;
  private final VisitService visitService;
  private final UserResolver userResolver;
  private static final Set<String> EDITABLE_GENDERS = Set.of("MALE", "FEMALE");
  private final AuthMapper authMapper;

  @GetMapping("/mypage")
  public String myPage(Model model) {
    Long userId = userResolver.requireCurrentUserId();
    User member =
        new User(
            1L,
            "user@greenfood.test",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            "이재우",
            "MALE",
            LocalDate.of(1995, 5, 20),
            LocalDateTime.now().minusMonths(6),
            null,
            null);
    List<Product> recommendedProducts =
        List.of(
            new Product(
                1L,
                8L,
                "청정 목장 무항생제 우유 900ml",
                "하루를 채워주는 산뜻한 선택",
                3900,
                2239,
                "ON_SALE",
                LocalDateTime.now().minusDays(8),
                null),
            new Product(
                2L,
                6L,
                "[1Table] 한우 나주곰탕 400g",
                "담백 깔끔한 맑은 국물",
                5760,
                831,
                "ON_SALE",
                LocalDateTime.now().minusDays(10),
                null),
            new Product(
                3L,
                6L,
                "350분만에 6일 패키지 도시락",
                "시간이 없다 싶을 때",
                6000,
                721,
                "ON_SALE",
                LocalDateTime.now().minusDays(12),
                null),
            new Product(
                4L,
                6L,
                "느리게 우려낸 한우 우거지탕 800g",
                "부드럽게 녹아드는 한우의 맛",
                13000,
                459,
                "ON_SALE",
                LocalDateTime.now().minusDays(15),
                null),
            new Product(
                5L,
                7L,
                "[비비드키친] 저당 닭가슴살 샐러드 3종 125g",
                "단백질 챙긴 가벼운 식사",
                3900,
                1064,
                "ON_SALE",
                LocalDateTime.now().minusDays(18),
                null));
    Set<Long> favoriteProductIds =
        favoriteService.findFavoriteProductIds(
            userId, recommendedProducts.stream().map(Product::id).toList());
    int purchaseCount = purchaseService.countPurchases(userId);
    int favoriteCount = favoriteService.countFavorites(userId);
    int visitCount = visitService.countVisits(userId);
    RecentPurchaseProductDto recentPurchaseProduct =
        purchaseService.findLatestPurchaseProduct(userId).orElse(null);
    Map<Long, String> productImageUrls =
        Map.of(
            1L,
            "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=320&q=80",
            2L,
            "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=320&q=80",
            3L,
            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=320&q=80",
            4L,
            "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?auto=format&fit=crop&w=320&q=80",
            5L,
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=320&q=80");

    model.addAttribute("contentTemplate", "mypage/index");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "mypage");
    model.addAttribute("member", member);
    model.addAttribute(
        "summaryCards",
        List.of(
            Map.of("label", "주문 상품", "count", purchaseCount),
            Map.of("label", "관심 상품", "count", favoriteCount),
            Map.of("label", "최근 본 상품", "count", visitCount)));
    model.addAttribute("recentPurchaseProduct", recentPurchaseProduct);
    model.addAttribute("productImageUrls", productImageUrls);
    model.addAttribute("recommendedProducts", recommendedProducts);
    model.addAttribute("favoriteProductIds", favoriteProductIds);
    return "layout/base";
  }

  @GetMapping("/mypage/edit")
  public String editMember(Model model) {
    Users member = findCurrentMember();
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
    Users member = findCurrentMember();
    if (member == null) {
      return "redirect:/login";
    }

    try {
      String normalizedName = validateAndNormalizeName(name);
      validateMemberEdit(birth, gender);

      int updated = authMapper.updateUserProfile(member.getId(), normalizedName, birth, gender);
      if (updated == 0) {
        throw new IllegalArgumentException("회원 정보를 수정할 수 없습니다.");
      }

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
    Users member = findCurrentMember();
    if (member == null) {
      throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
    }

    int withdrawn = authMapper.withdrawUser(member.getId());
    if (withdrawn == 0) {
      throw new GeneralException(ErrorStatus.MEMBER_WITHDRAW_FAILED);
    }

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

  private Users findCurrentMember() {
    Long userId = userResolver.currentUserIdOrNull();
    if (userId == null) {
      return null;
    }

    return authMapper.findById(userId);
  }

  private String validateAndNormalizeName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("이름을 입력해 주세요.");
    }

    String normalizedName = name.trim();
    if (normalizedName.length() > 100) {
      throw new IllegalArgumentException("이름은 100자 이하로 입력해 주세요.");
    }

    return normalizedName;
  }

  private void validateMemberEdit(LocalDate birth, String gender) {
    if (birth == null) {
      throw new IllegalArgumentException("생년월일을 입력해 주세요.");
    }

    if (birth.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("생년월일은 오늘 이후 날짜를 입력할 수 없습니다.");
    }

    if (!EDITABLE_GENDERS.contains(gender)) {
      throw new IllegalArgumentException("성별 값을 확인해 주세요.");
    }
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
