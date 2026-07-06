package kr.or.hieating.hotdeal.admin.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.or.hieating.ai.service.TargetSelectionJobRegistrar;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.hotdeal.admin.dto.HotDealCreateRequestDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealDetailResponseDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealResponseDTO;
import kr.or.hieating.hotdeal.admin.dto.HotDealUpdateRequestDTO;
import kr.or.hieating.hotdeal.admin.mapper.AdminHotDealMapper;
import kr.or.hieating.hotdeal.domain.HotDealProducts;
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

  // AI 비활성화 시에도 핫딜 등록이 가능하도록 선택적으로 Bean을 조회한다.
  // AI 활성화 시에는 핫딜과 Job을 같은 트랜잭션에 저장해 Job 유실을 방지한다.
  private final ObjectProvider<TargetSelectionJobRegistrar> targetSelectionJobRegistrar;

  @Transactional
  public List<HotDealResponseDTO> getExistingHotDeals() {
    adminHotDealMapper.updateStatusesByPeriod();
    return adminHotDealMapper.selectManageableHotDeals();
  }

  @Transactional
  public int createHotDeal(HotDealCreateRequestDTO request) {

    LocalDate today = LocalDate.now();

    if (request.getStartsAt().isBefore(today)) {
      throw new GeneralException(ErrorStatus.INVALID_START_DATE);
    }

    if (request.getEndsAt().isBefore(request.getStartsAt())) {
      throw new GeneralException(ErrorStatus.INVALID_END_DATE);
    }

    List<Integer> productOptionIds =
        request.getProducts().stream()
            .map(HotDealCreateRequestDTO.ProductItemDTO::getProductOptionId)
            .toList();
    if (adminHotDealMapper.countExpiredProductOptions(productOptionIds) > 0) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    String status = request.getStartsAt().isAfter(today) ? "SCHEDULED" : "ACTIVE";
    int adminUserId = Math.toIntExact(userResolver.requireCurrentUserId());

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
    int hotDealId = hotDeal.getId();

    // 할인비율 적용
    double discountMultiplier = (100 - request.getDiscountRate()) / 100.0;

    for (HotDealCreateRequestDTO.ProductItemDTO item : request.getProducts()) {
      int calculatedPrice = (int) (item.getOriginalPrice() * discountMultiplier);
      int finalHotDealPrice = (int) (Math.round(calculatedPrice / 10.0) * 10); // 10원 단위 반올림

      HotDealProducts child =
          HotDealProducts.builder()
              .hotDealId(hotDealId)
              .productOptionId(item.getProductOptionId())
              .originalPrice(item.getOriginalPrice())
              .hotDealPrice(finalHotDealPrice)
              .build();

      adminHotDealMapper.insertHotDealProduct(child);
    }

    // 원격 AI 호출은 Scheduler가 수행하며, 여기서는 처리할 DB Job만 등록한다.
    targetSelectionJobRegistrar.ifAvailable(registrar -> registrar.register(hotDealId));
    return hotDealId;
  }

  @Transactional
  public void updateHotDeal(int id, HotDealUpdateRequestDTO request) {
    HotDeals existing = adminHotDealMapper.selectHotDealById(id);
    if (existing == null) {
      throw new GeneralException(ErrorStatus.HOT_DEAL_NOT_FOUND);
    }

    LocalDate today = LocalDate.now();

    if (request.getStartsAt().isBefore(today)) {
      throw new GeneralException(ErrorStatus.INVALID_START_DATE);
    }

    if (request.getEndsAt().isBefore(request.getStartsAt())) {
      throw new GeneralException(ErrorStatus.INVALID_END_DATE);
    }

    List<Integer> requestedProductOptionIds =
        request.getProducts().stream()
            .map(HotDealUpdateRequestDTO.ProductItemDTO::getProductOptionId)
            .toList();
    if (adminHotDealMapper.countUnlinkedExpiredProductOptions(id, requestedProductOptionIds) > 0) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    String status = request.getStartsAt().isAfter(today) ? "SCHEDULED" : "ACTIVE";

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

    // 상품 리스트에 변경사항이 있는지 확인
    boolean productsUnchanged =
        isProductListUnchanged(existingProducts, request.getProducts(), request.getDiscountRate());

    // 핫딜에 포함된 상품들에 대한 변경사항이 있을 때, 삭제 후 재생성 진행
    if (!productsUnchanged) {
      adminHotDealMapper.deleteHotDealProductsByHotDealId(id);

      // 전달된 새로운 상품 목록으로 핫딜 상품 재등록
      double discountMultiplier = (100 - request.getDiscountRate()) / 100.0;

      for (HotDealUpdateRequestDTO.ProductItemDTO item : request.getProducts()) {
        int calculatedPrice = (int) (item.getOriginalPrice() * discountMultiplier);
        int finalHotDealPrice = (int) (Math.round(calculatedPrice / 10.0) * 10); // 10원 단위 반올림

        HotDealProducts child =
            HotDealProducts.builder()
                .hotDealId(id)
                .productOptionId(item.getProductOptionId())
                .originalPrice(item.getOriginalPrice())
                .hotDealPrice(finalHotDealPrice)
                .build();

        adminHotDealMapper.insertHotDealProduct(child);
      }
    }
  }

  private boolean isProductListUnchanged(
      List<HotDealDetailResponseDTO.ProductItemDTO> existing,
      List<HotDealUpdateRequestDTO.ProductItemDTO> requested,
      int discountRate) {
    if (existing == null || requested == null || existing.size() != requested.size()) {
      return false;
    }

    double discountMultiplier = (100 - discountRate) / 100.0;

    // 기존 상품 목록을 Map으로 변환하여 상품 ID로 빠른 조회를 위해 사용  (O(1) 조회)
    Map<Integer, HotDealDetailResponseDTO.ProductItemDTO> existingMap =
        existing.stream()
            .collect(
                Collectors.toMap(
                    HotDealDetailResponseDTO.ProductItemDTO::getProductOptionId,
                    Function.identity()));

    for (HotDealUpdateRequestDTO.ProductItemDTO reqItem : requested) {
      HotDealDetailResponseDTO.ProductItemDTO extItem =
          existingMap.get(reqItem.getProductOptionId());

      // 기존 DB에 없는 상품이 요청에 포함되어 있는 경우
      if (extItem == null) {
        return false; // 상품 구성이 바뀜
      }

      int calculatedPrice = (int) (reqItem.getOriginalPrice() * discountMultiplier);
      int finalHotDealPrice = (int) (Math.round(calculatedPrice / 10.0) * 10);

      // 기존 원가 혹은 할인율이 변경되었는지 확인
      if (!extItem.getOriginalPrice().equals(reqItem.getOriginalPrice())
          || !extItem.getHotDealPrice().equals(finalHotDealPrice)) {
        return false;
      }
    }
    return true;
  }

  public HotDealDetailResponseDTO getHotDealDetail(int id) {
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
      if (first.getOriginalPrice() > 0 && first.getHotDealPrice() != null) {
        discountRate = 100 - (first.getHotDealPrice() * 100 / first.getOriginalPrice());
      }
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
  public void deleteHotDeal(int id) {
    HotDeals hotDeal = adminHotDealMapper.selectHotDealById(id);
    if (hotDeal == null) {
      throw new GeneralException(ErrorStatus.HOT_DEAL_NOT_FOUND);
    }
    adminHotDealMapper.softDeleteHotDeal(id);
  }
}
