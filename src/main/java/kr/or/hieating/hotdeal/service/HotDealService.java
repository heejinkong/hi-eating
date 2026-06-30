package kr.or.hieating.hotdeal.service;

import java.util.List;
import kr.or.hieating.hotdeal.dto.HotDealProductsResponseDto;
import kr.or.hieating.hotdeal.mapper.HotDealMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotDealService {

  private final HotDealMapper hotDealMapper;

  public List<HotDealProductsResponseDto> findActiveHotDealProducts() {
    return hotDealMapper.findActiveHotDealProducts();
  }
}
