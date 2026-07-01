package kr.or.hieating.hotdeal.admin.mapper;

import java.util.List;
import kr.or.hieating.hotdeal.admin.dto.HotDealResponseDTO;
import kr.or.hieating.hotdeal.domain.HotDealProducts;
import kr.or.hieating.hotdeal.domain.HotDeals;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminHotDealMapper {

  List<HotDealResponseDTO> selectManageableHotDeals();

  void insertHotDeal(HotDeals hotDeal);

  void insertHotDealProduct(HotDealProducts hotDealProduct);
}
