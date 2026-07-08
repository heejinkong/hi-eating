package kr.or.hieating.hotdeal.mapper;

import java.util.List;
import kr.or.hieating.hotdeal.dto.ActiveHotDealResponseDto;
import kr.or.hieating.hotdeal.dto.HotDealProductListItemResponseDto;
import kr.or.hieating.hotdeal.dto.HotDealProductSearchCondition;
import kr.or.hieating.hotdeal.dto.HotDealProductsResponseDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotDealMapper {

  List<HotDealProductsResponseDto> findActiveHotDealProducts();

  List<ActiveHotDealResponseDto> findActiveHotDeals();

  List<HotDealProductListItemResponseDto> findHotDealProducts(
      HotDealProductSearchCondition condition);

  int countHotDealProducts(HotDealProductSearchCondition condition);
}
