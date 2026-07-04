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
      String keyword, Long categoryId, String sortBy, int page, int size) {
    String normalizedKeyword = normalizeKeyword(keyword);
    int offset = (page - 1) * size;
    List<ProductSearchResponseDTO> list =
        adminProductMapper.searchProductsForHotDeal(
            normalizedKeyword, categoryId, sortBy, offset, size);
    int totalCount = adminProductMapper.countProductsForHotDeal(normalizedKeyword, categoryId);
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

  private String normalizeKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    return keyword.trim();
  }
}
