package kr.or.hieating.hotdeal.mapper;

import java.util.List;
import kr.or.hieating.hotdeal.dto.HotDealProductsResponseDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotDealMapper {

  List<HotDealProductsResponseDto> findActiveHotDealProducts();
}
