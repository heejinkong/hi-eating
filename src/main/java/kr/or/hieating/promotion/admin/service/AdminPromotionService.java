package kr.or.hieating.promotion.admin.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.promotion.admin.config.PromotionImageUploadClient;
import kr.or.hieating.promotion.admin.dto.PromotionReorderRequestDTO;
import kr.or.hieating.promotion.admin.mapper.AdminPromotionMapper;
import kr.or.hieating.promotion.domain.Promotions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPromotionService {

  private final AdminPromotionMapper adminPromotionMapper;
  private final PromotionImageUploadClient promotionImageUploadClient;

  public List<Promotions> getAllPromotions() {
    List<Promotions> promotions = adminPromotionMapper.selectAllPromotions();
    return promotions != null ? promotions : List.of();
  }

  @Transactional
  public Promotions registerPromotion(
      MultipartFile file, String title, String link, LocalDate startsAt, LocalDate endsAt) {
    // 파일 업로드 수행 (중복 코드 제거, 확장자/Content-Type 화이트리스트 검증 포함)
    String imgSrc = promotionImageUploadClient.upload(file);

    Promotions promotion =
        Promotions.builder()
            .title(title)
            .imgSrc(imgSrc)
            .link(link)
            // display_order는 insert SQL 내 서브쿼리 SELECT COALESCE(MAX(display_order), 0) + 1000 를 통해
            // 원자적으로 계산
            .startsAt(startsAt.atStartOfDay())
            .endsAt(endsAt.atTime(23, 59, 59))
            .build();

    adminPromotionMapper.insertPromotion(promotion);
    return promotion;
  }

  @Transactional
  public void updatePromotionDetails(
      int id, MultipartFile file, String title, String link, LocalDate startsAt, LocalDate endsAt) {
    String newImgSrc = null;
    Promotions existingPromotion = adminPromotionMapper.selectPromotionById(id);
    if (existingPromotion == null) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    // 새로운 이미지 파일이 전달된 경우 업로드 처리
    if (file != null && !file.isEmpty()) {
      newImgSrc = promotionImageUploadClient.upload(file);
    }

    Promotions promotion =
        Promotions.builder()
            .id(id)
            .title(title)
            .link(link)
            .imgSrc(newImgSrc)
            .startsAt(startsAt.atStartOfDay())
            .endsAt(endsAt.atTime(23, 59, 59))
            .build();

    adminPromotionMapper.updatePromotion(promotion);
    if (newImgSrc != null) {
      promotionImageUploadClient.delete(existingPromotion.getImgSrc());
    }
  }

  @Transactional
  public void deletePromotion(int id) {
    Promotions promotion = adminPromotionMapper.selectPromotionById(id);
    if (promotion == null) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    promotionImageUploadClient.delete(promotion.getImgSrc());
    adminPromotionMapper.deletePromotion(id);
  }

  @Transactional
  public void reorderPromotions(PromotionReorderRequestDTO request) {
    List<Integer> adjacentIds =
        Stream.of(
                request.getMovedPromotionId(),
                request.getPreviousPromotionId(),
                request.getNextPromotionId())
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();

    List<Promotions> adjacentPromotions =
        adminPromotionMapper.selectPromotionsByIdsForUpdate(adjacentIds);

    if (adjacentPromotions.size() != adjacentIds.size()) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    Map<Integer, Promotions> promotionById = new HashMap<>();
    adjacentPromotions.forEach(promotion -> promotionById.put(promotion.getId(), promotion)); //

    Integer previousId = request.getPreviousPromotionId();
    Integer nextId = request.getNextPromotionId();

    if (previousId == null && nextId == null) {
      return;
    }

    if (previousId == null) {
      int newOrder = promotionById.get(nextId).getDisplayOrder() - 1000;
      adminPromotionMapper.updateDisplayOrder(request.getMovedPromotionId(), newOrder);
      return;
    }

    if (nextId == null) {
      int newOrder = promotionById.get(previousId).getDisplayOrder() + 1000;
      adminPromotionMapper.updateDisplayOrder(request.getMovedPromotionId(), newOrder);
      return;
    }

    int previousOrder = promotionById.get(previousId).getDisplayOrder();
    int nextOrder = promotionById.get(nextId).getDisplayOrder();
    if (nextOrder - previousOrder > 1) {
      int newOrder = previousOrder + (nextOrder - previousOrder) / 2;
      adminPromotionMapper.updateDisplayOrder(request.getMovedPromotionId(), newOrder);
      return;
    }

    rebalanceDisplayOrders(request.getOrderedPromotionIds());
  }

  // 1점보다 작을 경우 타는 함수
  private void rebalanceDisplayOrders(List<Integer> orderedPromotionIds) {
    List<Integer> storedPromotionIds = adminPromotionMapper.selectAllPromotionIdsForUpdate();
    if (storedPromotionIds.size() != orderedPromotionIds.size()
        || !storedPromotionIds.containsAll(orderedPromotionIds)) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    // UNIQUE 제약조건과 기존 순서값이 충돌하지 않도록 먼저 모든 값을 임시 영역으로 이동합니다.
    adminPromotionMapper.moveDisplayOrdersToTemporaryRange();

    for (int index = 0; index < orderedPromotionIds.size(); index++) {
      adminPromotionMapper.updateDisplayOrder(orderedPromotionIds.get(index), (index + 1) * 1000);
    }
  }
}
