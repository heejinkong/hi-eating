package kr.or.hieating.product.admin.service;

import java.util.List;
import kr.or.hieating.product.admin.dto.CategoryResponseDTO;
import kr.or.hieating.product.admin.dto.ProductPageResponseDTO;
import kr.or.hieating.product.admin.dto.ProductSearchResponseDTO;
import kr.or.hieating.product.admin.mapper.AdminProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

  private final AdminProductMapper adminProductMapper;

  public ProductPageResponseDTO searchProducts(
      String keyword, Long categoryId, Integer hotDealId, String sortBy, int page, int size) {
    int offset = (page - 1) * size;
    List<ProductSearchResponseDTO> list =
        adminProductMapper.searchProductsForHotDeal(
            keyword, categoryId, hotDealId, sortBy, offset, size);
    int totalCount = adminProductMapper.countProductsForHotDeal(keyword, categoryId, hotDealId);
    int totalPages = (int) Math.ceil((double) totalCount / size);

    return ProductPageResponseDTO.builder()
        .list(list)
        .totalCount(totalCount)
        .totalPages(totalPages)
        .currentPage(page)
        .build();
  }

  public List<CategoryResponseDTO> getAllCategories() {
    return adminProductMapper.selectAllCategories();
  }
}
