package kr.or.hieating.hotdeal.admin.service;

import java.time.LocalDate;
import java.util.List;
import kr.or.hieating.ai.service.TargetSelectionJobRegistrar;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.hotdeal.admin.dto.HotDealCreateRequestDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealDetailResponseDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealResponseDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealUpdateRequestDTO;
import kr.or.hieating.hotdeal.admin.mapper.AdminHotDealMapper;
import kr.or.hieating.hotdeal.domain.HotDeals;
import kr.or.hieating.utils.UserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminHotDealService {

  private final AdminHotDealMapper adminHotDealMapper;
  private final UserResolver userResolver;
  private final HotDealPricePolicy pricePolicy;
  private final HotDealProductSyncService productSyncService;

  // AI 비활성화 시에도 핫딜 등록이 가능하도록 선택적으로 Bean을 조회한다.
  // AI 활성화 시에는 핫딜과 Job을 같은 트랜잭션에 저장해 Job 유실을 방지한다.
  private final ObjectProvider<TargetSelectionJobRegistrar> targetSelectionJobRegistrar;

  @Transactional
  public List<HotDealResponseDTO> getExistingHotDeals() {
    adminHotDealMapper.updateStatusesByPeriod();
    return adminHotDealMapper.selectManageableHotDeals();
  }

  @Transactional
  public Long createHotDeal(HotDealCreateRequestDTO request) {

    LocalDate today = LocalDate.now();
    pricePolicy.validatePeriod(request.getStartsAt(), request.getEndsAt(), today);

    List<Long> productOptionIds =
        request.getProducts().stream()
            .map(HotDealCreateRequestDTO.ProductItemDTO::getProductOptionId)
            .map(Integer::longValue)
            .toList();
    if (adminHotDealMapper.countExpiredProductOptions(productOptionIds) > 0) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    String status = pricePolicy.determineStatus(request.getStartsAt(), today);
    Long adminUserId = userResolver.requireCurrentUserId();

    HotDeals hotDeal =
        HotDeals.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .startsAt(request.getStartsAt().atStartOfDay())
            .endsAt(request.getEndsAt().atTime(23, 59, 59))
            .status(status)
            .createdBy(adminUserId)
            .build();

    adminHotDealMapper.insertHotDeal(hotDeal);
    Long hotDealId = hotDeal.getId();

    productSyncService.insertCreatedProducts(
        hotDealId, request.getProducts(), request.getDiscountRate());

    // 원격 AI 호출은 Scheduler가 수행하며, 여기서는 처리할 DB Job만 등록한다.
    targetSelectionJobRegistrar.ifAvailable(registrar -> registrar.register(hotDealId));
    return hotDealId;
  }

  @Transactional
  public void updateHotDeal(Long id, HotDealUpdateRequestDTO request) {
    HotDeals existing = adminHotDealMapper.selectHotDealById(id);
    if (existing == null) {
      throw new GeneralException(ErrorStatus.HOT_DEAL_NOT_FOUND);
    }

    LocalDate today = LocalDate.now();
    pricePolicy.validatePeriod(request.getStartsAt(), request.getEndsAt(), today);

    List<Long> requestedProductOptionIds =
        request.getProducts().stream()
            .map(HotDealUpdateRequestDTO.ProductItemDTO::getProductOptionId)
            .map(Integer::longValue)
            .toList();
    if (adminHotDealMapper.countUnlinkedExpiredProductOptions(id, requestedProductOptionIds) > 0) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    String status = pricePolicy.determineStatus(request.getStartsAt(), today);

    HotDeals hotDeal =
        HotDeals.builder()
            .id(id)
            .title(request.getTitle())
            .description(request.getDescription())
            .startsAt(request.getStartsAt().atStartOfDay())
            .endsAt(request.getEndsAt().atTime(23, 59, 59))
            .status(status)
            .build();

    adminHotDealMapper.updateHotDeal(hotDeal);

    // 기존 등록된 상품 목록 조회
    List<HotDealDetailResponseDTO.ProductItemDTO> existingProducts =
        adminHotDealMapper.selectHotDealProductsDetailByHotDealId(id);

    productSyncService.replaceUpdatedProductsIfChanged(
        id, existingProducts, request.getProducts(), request.getDiscountRate());
  }

  public HotDealDetailResponseDTO getHotDealDetail(Long id) {
    HotDeals hotDeal = adminHotDealMapper.selectHotDealById(id);
    if (hotDeal == null) {
      throw new GeneralException(ErrorStatus.HOT_DEAL_NOT_FOUND);
    }

    List<HotDealDetailResponseDTO.ProductItemDTO> products =
        adminHotDealMapper.selectHotDealProductsDetailByHotDealId(id);

    // 첫번째 상품 기준으로 할인율 계산
    int discountRate = 0;
    if (products != null && !products.isEmpty()) {
      HotDealDetailResponseDTO.ProductItemDTO first = products.get(0);
      discountRate =
          pricePolicy.calculateDiscountRate(first.getOriginalPrice(), first.getHotDealPrice());
    }

    return HotDealDetailResponseDTO.builder()
        .id(hotDeal.getId())
        .title(hotDeal.getTitle())
        .description(hotDeal.getDescription())
        .startsAt(hotDeal.getStartsAt())
        .endsAt(hotDeal.getEndsAt())
        .discountRate(discountRate)
        .products(products)
        .build();
  }

  @Transactional
  public void deleteHotDeal(Long id) {
    HotDeals hotDeal = adminHotDealMapper.selectHotDealById(id);
    if (hotDeal == null) {
      throw new GeneralException(ErrorStatus.HOT_DEAL_NOT_FOUND);
    }
    adminHotDealMapper.softDeleteHotDeal(id);
  }
}
