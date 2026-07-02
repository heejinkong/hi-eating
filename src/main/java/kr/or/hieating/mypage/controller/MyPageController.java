package kr.or.hieating.mypage.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.or.hieating.favorite.domain.Favorite;
import kr.or.hieating.favorite.service.FavoriteService;
import kr.or.hieating.product.domain.Product;
import kr.or.hieating.purchase.domain.Purchase;
import kr.or.hieating.user.domain.User;
import kr.or.hieating.utils.UserResolver;
import kr.or.hieating.visit.domain.Visit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MyPageController {

  private final FavoriteService favoriteService;
  private final UserResolver userResolver;

  @GetMapping("/mypage")
  public String myPage(Model model) {
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
    Product recentOrderProduct =
        new Product(
            11L,
            6L,
            "아침 그래놀라 오트밀 세트",
            "든든한 아침 식사를 위한 오트밀 세트",
            119640,
            128,
            "ON_SALE",
            LocalDateTime.now().minusDays(30),
            null);
    Purchase recentPurchase =
        new Purchase(
            401L,
            member.id(),
            101L,
            1,
            recentOrderProduct.price(),
            LocalDateTime.now().minusDays(3),
            null);
    List<Purchase> purchases = List.of(recentPurchase);
    List<Favorite> favorites =
        List.of(new Favorite(member.id(), 1L, LocalDateTime.now().minusDays(1), null));
    List<Visit> visits =
        List.of(new Visit(member.id(), recentOrderProduct.id(), LocalDateTime.now(), null));
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
            userResolver.currentUserId(), recommendedProducts.stream().map(Product::id).toList());
    Map<Long, String> productImageUrls =
        Map.of(
            recentOrderProduct.id(),
            "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?auto=format&fit=crop&w=320&q=80",
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
            Map.of("label", "주문 상품", "count", purchases.size()),
            Map.of("label", "관심 상품", "count", favorites.size()),
            Map.of("label", "최근 본 상품", "count", visits.size())));
    model.addAttribute("recentPurchase", recentPurchase);
    model.addAttribute("recentOrderProduct", recentOrderProduct);
    model.addAttribute("productImageUrls", productImageUrls);
    model.addAttribute("recommendedProducts", recommendedProducts);
    model.addAttribute("favoriteProductIds", favoriteProductIds);
    return "layout/base";
  }
}
