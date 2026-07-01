package kr.or.hieating.product.controller;

import java.util.List;
import kr.or.hieating.category.dto.CategoryMenuResponseDto;
import kr.or.hieating.category.service.CategoryService;
import kr.or.hieating.product.domain.ProductDetail;
import kr.or.hieating.product.dto.ProductListPageResponseDto;
import kr.or.hieating.product.dto.ProductListSearchCondition;
import kr.or.hieating.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;
  private final CategoryService categoryService;

  @GetMapping("/product/{id}")
  public String detail(@PathVariable Long id, Model model) {
    ProductDetail product =
        productService
            .findProductDetail(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    model.addAttribute("contentTemplate", "product/detail");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "product-detail");
    model.addAttribute("pageScript", "product-detail");
    model.addAttribute("product", product);
    return "layout/base";
  }

  @GetMapping("/product")
  public String mostPurchasedProducts(Model model) {
    model.addAttribute("products", productService.findMostPurchasedProducts());
    return "product/products";
  }

  @GetMapping("/categories/{categoryId}")
  public String categoryProducts(
      @PathVariable Long categoryId,
      @RequestParam(required = false) Integer minPrice,
      @RequestParam(required = false) Integer maxPrice,
      @RequestParam(required = false) Integer minDiscountRate,
      @RequestParam(defaultValue = "popular") String sort,
      @RequestParam(defaultValue = "1") Integer page,
      Model model) {
    ProductListSearchCondition condition =
        new ProductListSearchCondition(categoryId, minPrice, maxPrice, minDiscountRate, sort, page);
    ProductListPageResponseDto productPage = productService.findProductsByCategory(condition);
    CategoryMenuResponseDto selectedCategory = categoryService.findCategoryById(categoryId);
    List<CategoryMenuResponseDto> categories = categoryService.findCategories();

    model.addAttribute("contentTemplate", "product/list");
    model.addAttribute("contentFragment", "content");
    model.addAttribute("pageStylesheet", "product-list");
    model.addAttribute("categories", categories);
    model.addAttribute("selectedCategory", selectedCategory);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("condition", condition);
    model.addAttribute("productPage", productPage);
    return "layout/base";
  }
}
