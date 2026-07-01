package kr.or.hieating.product.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.or.hieating.product.domain.ProductDetail;
import kr.or.hieating.product.domain.ProductOption;
import kr.or.hieating.product.service.ProductService;
import kr.or.hieating.review.domain.ProductReview;
import kr.or.hieating.review.domain.ReviewSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping("/product/{id}")
  public String detail(@PathVariable Long id, Model model) {
    ProductDetail product = sampleProduct(id);

    model.addAttribute("contentTemplate", "product/detail");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "product-detail");
    model.addAttribute("product", product);
    model.addAttribute("reviews", sampleReviews(product.id()));
    return "layout/base";
  }

  @GetMapping("/product")
  public String mostPurchasedProducts(Model model) {
    model.addAttribute("products", productService.findMostPurchasedProducts());
    return "product/products";
  }

  private ProductDetail sampleProduct(Long id) {
    return new ProductDetail(
        id,
        8L,
        "음료/차",
        "청정 목장 무항생제 우유 900ml",
        """
            <h2>청정 목장 무항생제 우유 900ml</h2>
            <p><strong>매일 새벽 목장에서 받은 신선한 원유</strong>를 담백하게 살린 무항생제 우유입니다.</p>
            <ul>
              <li>고소한 풍미와 깔끔한 목넘김</li>
              <li>아침 식사, 시리얼, 커피에 잘 어울리는 기본 우유</li>
              <img src="https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=900&q=85" />
            </ul>
            """,
        3900,
        2239,
        "ON_SALE",
        LocalDateTime.now().minusDays(8),
        List.of(
            "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=900&q=85",
            "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=360&q=80",
            "https://images.unsplash.com/photo-1600788907416-456578634209?auto=format&fit=crop&w=360&q=80",
            "https://images.unsplash.com/photo-1628088062854-d1870b4553da?auto=format&fit=crop&w=360&q=80"),
        List.of(
            new ProductOption(101L, id, 42, LocalDate.now().plusDays(7)),
            new ProductOption(102L, id, 18, LocalDate.now().plusDays(10))),
        new ReviewSummary(4.8, 2239),
        true);
  }

  private List<ProductReview> sampleReviews(Long productId) {
    return List.of(
        new ProductReview(
            1L,
            7L,
            productId,
            301L,
            "김하늘",
            5,
            "우유 비린 맛이 적고 고소해서 아침마다 잘 마시고 있어요. 포장도 탄탄하게 도착했습니다.",
            "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=420&q=80",
            LocalDateTime.now().minusDays(2),
            "900ml / 기본"),
        new ProductReview(
            2L,
            12L,
            productId,
            309L,
            "박민준",
            5,
            "시리얼에 넣어 먹기 좋고 커피에 섞어도 맛이 깔끔합니다. 재구매할 것 같아요.",
            null,
            LocalDateTime.now().minusDays(5),
            "900ml / 기본"),
        new ProductReview(
            3L,
            18L,
            productId,
            314L,
            "이서연",
            4,
            "배송이 빨랐고 유통기한도 넉넉했습니다. 아이가 잘 마셔서 만족해요.",
            "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=420&q=80",
            LocalDateTime.now().minusDays(9),
            "900ml / 기본"));
  }
}
