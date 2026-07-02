package kr.or.hieating.hotdeal.admin.mapper;

import java.util.List;
import kr.or.hieating.hotdeal.admin.dto.HotDealDetailResponseDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealResponseDTO;
import kr.or.hieating.hotdeal.domain.HotDealProducts;
import kr.or.hieating.hotdeal.domain.HotDeals;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminHotDealMapper {

  List<HotDealResponseDTO> selectManageableHotDeals();

  void insertHotDeal(HotDeals hotDeal);

  void insertHotDealProduct(HotDealProducts hotDealProduct);

  void updateHotDeal(HotDeals hotDeal);

  // 특정 핫딜에 연동된 상품들 삭제 (초기화)
  void deleteHotDealProductsByHotDealId(int hotDealId);

  HotDeals selectHotDealById(int id);

  List<HotDealDetailResponseDTO.ProductItemDTO> selectHotDealProductsDetailByHotDealId(
      int hotDealId);

  void softDeleteHotDeal(@Param("id") int id);
}
