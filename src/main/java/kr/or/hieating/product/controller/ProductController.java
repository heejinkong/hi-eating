package kr.or.hieating.product.controller;

import kr.or.hieating.product.domain.ProductDetail;
import kr.or.hieating.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

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
}
