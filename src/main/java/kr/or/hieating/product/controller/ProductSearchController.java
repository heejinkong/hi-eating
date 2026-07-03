package kr.or.hieating.product.controller;

import java.util.List;
import kr.or.hieating.category.dto.CategoryMenuResponseDto;
import kr.or.hieating.category.service.CategoryService;
import kr.or.hieating.product.dto.ProductListPageResponseDto;
import kr.or.hieating.product.dto.ProductSearchCondition;
import kr.or.hieating.product.service.ProductSearchService;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductSearchController {

  private final ProductSearchService productSearchService;
  private final CategoryService categoryService;
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
    List<CategoryMenuResponseDto> categories = categoryService.findCategories();

    model.addAttribute("contentTemplate", "search/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "product-list");
    model.addAttribute("categories", categories);
    model.addAttribute("condition", condition);
    model.addAttribute("keyword", condition.getKeyword());
    model.addAttribute("productPage", productPage);
    return "layout/base";
  }
}
