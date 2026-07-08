package kr.or.hieating.product.service;

import java.util.List;
import kr.or.hieating.product.dto.ProductListItemResponseDto;
import kr.or.hieating.product.dto.ProductListPageResponseDto;
import kr.or.hieating.product.dto.ProductSearchCondition;
import kr.or.hieating.product.mapper.ProductSearchMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

  private final ProductSearchMapper productSearchMapper;
  private final ImageUrlResolver imageUrlResolver;

  public ProductListPageResponseDto searchProducts(ProductSearchCondition condition) {
    if (!condition.hasKeyword()) {
      return new ProductListPageResponseDto(
          List.of(), condition.getPage(), condition.getSize(), 0, 1);
    }

    int totalCount = productSearchMapper.countSearchProducts(condition);
    int totalPages = Math.max((int) Math.ceil((double) totalCount / condition.getSize()), 1);
    ProductSearchCondition queryCondition =
        condition.getPage() > totalPages
            ? new ProductSearchCondition(
                condition.getKeyword(),
                condition.getUserId(),
                condition.getMinPrice(),
                condition.getMaxPrice(),
                condition.getMinDiscountRate(),
                condition.getSort(),
                totalPages)
            : condition;
    List<ProductListItemResponseDto> products = productSearchMapper.searchProducts(queryCondition);

    products.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));

    return new ProductListPageResponseDto(
        products, queryCondition.getPage(), queryCondition.getSize(), totalCount, totalPages);
  }
}
