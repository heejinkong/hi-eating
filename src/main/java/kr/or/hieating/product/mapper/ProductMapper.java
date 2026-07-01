package kr.or.hieating.product.mapper;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.product.domain.ProductOption;
import kr.or.hieating.product.dto.MostPurchasedProductResponseDto;
import kr.or.hieating.product.dto.ProductDetailRowDto;
import kr.or.hieating.product.dto.ProductListItemResponseDto;
import kr.or.hieating.product.dto.ProductListSearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper {

  List<MostPurchasedProductResponseDto> findMostPurchasedProducts();

  Optional<ProductDetailRowDto> findProductDetailRow(@Param("productId") Long productId);

  List<String> findProductImageUrls(@Param("productId") Long productId);

  List<ProductOption> findProductOptions(@Param("productId") Long productId);

  List<ProductListItemResponseDto> findProductsByCategory(ProductListSearchCondition condition);

  int countProductsByCategory(ProductListSearchCondition condition);
}
