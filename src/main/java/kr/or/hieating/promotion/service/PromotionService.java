package kr.or.hieating.promotion.service;

import java.util.List;
import kr.or.hieating.promotion.domain.Promotions;
import kr.or.hieating.promotion.mapper.PromotionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionService {

  private final PromotionMapper promotionMapper;

  public List<Promotions> findActivePromotions() {
    return promotionMapper.findActivePromotions();
  }
}
