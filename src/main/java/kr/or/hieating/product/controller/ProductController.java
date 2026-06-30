package kr.or.hieating.product.controller;

import kr.or.hieating.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @GetMapping("/product")
  public String mostPurchasedProducts(Model model) {

    model.addAttribute("products", productService.findMostPurchasedProducts());
    return "product/products";
  }
}
