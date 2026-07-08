package kr.or.hieating.hotdeal.service;

import java.util.List;
import kr.or.hieating.hotdeal.dto.ActiveHotDealResponseDto;
import kr.or.hieating.hotdeal.dto.HotDealProductListItemResponseDto;
import kr.or.hieating.hotdeal.dto.HotDealProductListPageResponseDto;
import kr.or.hieating.hotdeal.dto.HotDealProductSearchCondition;
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

  public List<ActiveHotDealResponseDto> findActiveHotDeals() {
    List<ActiveHotDealResponseDto> hotDeals = hotDealMapper.findActiveHotDeals();
    hotDeals.forEach(
        hotDeal ->
            hotDeal.setHeroImageLocation(imageUrlResolver.resolve(hotDeal.getHeroImageLocation())));
    return hotDeals;
  }

  public HotDealProductListPageResponseDto findHotDealProducts(
      HotDealProductSearchCondition condition) {
    int totalCount = hotDealMapper.countHotDealProducts(condition);
    int totalPages = (int) Math.ceil((double) totalCount / condition.getSize());

    List<HotDealProductListItemResponseDto> products =
        totalCount == 0 ? List.of() : hotDealMapper.findHotDealProducts(condition);

    products.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));

    return new HotDealProductListPageResponseDto(
        products, condition.getPage(), condition.getSize(), totalCount, totalPages);
  }
}
