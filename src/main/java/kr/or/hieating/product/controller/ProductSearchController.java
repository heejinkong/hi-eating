package kr.or.hieating.product.controller;

import kr.or.hieating.product.dto.ProductListPageResponseDto;
import kr.or.hieating.product.dto.ProductSearchCondition;
import kr.or.hieating.product.service.ProductSearchService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class ProductSearchController {

  private final ProductSearchService productSearchService;
  private final UserResolver userResolver;

  @GetMapping("/search")
  public String searchProducts(
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(required = false) Integer minPrice,
      @RequestParam(required = false) Integer maxPrice,
      @RequestParam(required = false) Integer minDiscountRate,
      @RequestParam(defaultValue = "popular") String sort,
      @RequestParam(defaultValue = "1") Integer page,
      Model model) {
    if (page == null || page < 1) {
      return redirectSearchProducts(keyword, minPrice, maxPrice, minDiscountRate, sort, 1);
    }

    ProductSearchCondition condition =
        new ProductSearchCondition(
            keyword,
            userResolver.currentUserIdOrNull(),
            minPrice,
            maxPrice,
            minDiscountRate,
            sort,
            page);
    ProductListPageResponseDto productPage = productSearchService.searchProducts(condition);
    if (page > productPage.totalPages()) {
      return redirectSearchProducts(
          condition.getKeyword(),
          condition.getMinPrice(),
          condition.getMaxPrice(),
          condition.getMinDiscountRate(),
          condition.getSort(),
          productPage.totalPages());
    }

    model.addAttribute("contentTemplate", "search/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "product-list");
    model.addAttribute("condition", condition);
    model.addAttribute("keyword", condition.getKeyword());
    model.addAttribute("productPage", productPage);
    return "layout/base";
  }

  private String redirectSearchProducts(
      String keyword,
      Integer minPrice,
      Integer maxPrice,
      Integer minDiscountRate,
      String sort,
      int page) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromPath("/search")
            .queryParam("page", page)
            .queryParam("sort", sort == null ? "popular" : sort);

    addQueryParamIfPresent(builder, "keyword", keyword);
    addQueryParamIfPresent(builder, "minPrice", minPrice);
    addQueryParamIfPresent(builder, "maxPrice", maxPrice);
    addQueryParamIfPresent(builder, "minDiscountRate", minDiscountRate);

    return "redirect:" + builder.build().encode().toUriString();
  }

  private void addQueryParamIfPresent(UriComponentsBuilder builder, String name, Integer value) {
    if (value != null) {
      builder.queryParam(name, value);
    }
  }

  private void addQueryParamIfPresent(UriComponentsBuilder builder, String name, String value) {
    if (value != null && !value.isBlank()) {
      builder.queryParam(name, value.trim());
    }
  }
}
