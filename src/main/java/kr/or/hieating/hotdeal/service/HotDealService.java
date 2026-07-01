package kr.or.hieating.hotdeal.service;

import java.util.List;
import kr.or.hieating.hotdeal.dto.HotDealProductsResponseDto;
import kr.or.hieating.hotdeal.mapper.HotDealMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotDealService {

  private final HotDealMapper hotDealMapper;
  private final ImageUrlResolver imageUrlResolver;

  public List<HotDealProductsResponseDto> findActiveHotDealProducts() {
    List<HotDealProductsResponseDto> hotDealProducts = hotDealMapper.findActiveHotDealProducts();
    hotDealProducts.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));
    return hotDealProducts;
  }
}
