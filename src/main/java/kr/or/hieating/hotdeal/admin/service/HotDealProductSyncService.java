package kr.or.hieating.hotdeal.admin.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.or.hieating.hotdeal.admin.dto.HotDealCreateRequestDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealDetailResponseDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealUpdateRequestDTO;
import kr.or.hieating.hotdeal.admin.mapper.AdminHotDealMapper;
import kr.or.hieating.hotdeal.domain.HotDealProducts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotDealProductSyncService {

  private final AdminHotDealMapper adminHotDealMapper;
  private final HotDealPricePolicy pricePolicy;

  public void insertCreatedProducts(
      Long hotDealId, List<HotDealCreateRequestDTO.ProductItemDTO> products, int discountRate) {
    for (HotDealCreateRequestDTO.ProductItemDTO item : products) {
      adminHotDealMapper.insertHotDealProduct(
          createHotDealProduct(
              hotDealId,
              item.getProductOptionId().longValue(),
              item.getOriginalPrice(),
              discountRate));
    }
  }

  public void replaceUpdatedProductsIfChanged(
      Long hotDealId,
      List<HotDealDetailResponseDTO.ProductItemDTO> existingProducts,
      List<HotDealUpdateRequestDTO.ProductItemDTO> requestedProducts,
      int discountRate) {
    if (isProductListUnchanged(existingProducts, requestedProducts, discountRate)) {
      return;
    }

    adminHotDealMapper.deleteHotDealProductsByHotDealId(hotDealId);
    for (HotDealUpdateRequestDTO.ProductItemDTO item : requestedProducts) {
      adminHotDealMapper.insertHotDealProduct(
          createHotDealProduct(
              hotDealId,
              item.getProductOptionId().longValue(),
              item.getOriginalPrice(),
              discountRate));
    }
  }

  private boolean isProductListUnchanged(
      List<HotDealDetailResponseDTO.ProductItemDTO> existing,
      List<HotDealUpdateRequestDTO.ProductItemDTO> requested,
      int discountRate) {
    if (existing == null || requested == null || existing.size() != requested.size()) {
      return false;
    }

    Map<Long, HotDealDetailResponseDTO.ProductItemDTO> existingMap =
        existing.stream()
            .collect(
                Collectors.toMap(
                    item -> item.getProductOptionId().longValue(), Function.identity()));

    for (HotDealUpdateRequestDTO.ProductItemDTO requestedItem : requested) {
      HotDealDetailResponseDTO.ProductItemDTO existingItem =
          existingMap.get(requestedItem.getProductOptionId().longValue());
      if (existingItem == null) {
        return false;
      }

      int finalHotDealPrice =
          pricePolicy.calculateHotDealPrice(requestedItem.getOriginalPrice(), discountRate);
      if (!Objects.equals(existingItem.getOriginalPrice(), requestedItem.getOriginalPrice())
          || !Objects.equals(existingItem.getHotDealPrice(), finalHotDealPrice)) {
        return false;
      }
    }

    return true;
  }

  private HotDealProducts createHotDealProduct(
      Long hotDealId, Long productOptionId, int originalPrice, int discountRate) {
    return HotDealProducts.builder()
        .hotDealId(hotDealId)
        .productOptionId(productOptionId)
        .originalPrice(originalPrice)
        .hotDealPrice(pricePolicy.calculateHotDealPrice(originalPrice, discountRate))
        .build();
  }
}
