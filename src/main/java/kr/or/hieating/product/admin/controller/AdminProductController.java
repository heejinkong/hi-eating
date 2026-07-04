package kr.or.hieating.product.admin.controller;

import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.product.admin.dto.ProductPageResponseDTO;
import kr.or.hieating.product.admin.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/products")
public class AdminProductController {

  private final AdminProductService adminProductService;

  @GetMapping
  public ApiResponse<ProductPageResponseDTO> searchProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Integer hotDealId,
      @RequestParam(defaultValue = "EXPIRE_ASC") String sortBy,
      @RequestParam(defaultValue = "1") int page) {

    if (!"EXPIRE_ASC".equals(sortBy) && !"STOCK_ASC".equals(sortBy)) {
      throw new GeneralException(ErrorStatus.INVALID_SORT_BY);
    }

    int size = 5; // Set page size to 5
    ProductPageResponseDTO result =
        adminProductService.searchProducts(keyword, categoryId, hotDealId, sortBy, page, size);

    return ApiResponse.onSuccess(result);
  }
}
