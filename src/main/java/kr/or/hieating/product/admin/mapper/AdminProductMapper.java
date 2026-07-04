package kr.or.hieating.product.admin.mapper;

import java.util.List;
import kr.or.hieating.product.admin.dto.CategoryResponseDTO;
import kr.or.hieating.product.admin.dto.ProductSearchResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminProductMapper {
  List<ProductSearchResponseDTO> searchProductsForHotDeal(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("hotDealId") Integer hotDealId,
      @Param("sortBy") String sortBy,
      @Param("offset") int offset,
      @Param("size") int size);

  int countProductsForHotDeal(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("hotDealId") Integer hotDealId);

  List<CategoryResponseDTO> selectAllCategories();
}
