package kr.or.hieating.visit.service;

import kr.or.hieating.utils.ImageUrlResolver;
import kr.or.hieating.visit.dto.VisitProductListPageResponseDto;
import kr.or.hieating.visit.dto.VisitProductListSearchCondition;
import kr.or.hieating.visit.mapper.VisitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitService {

  private final VisitMapper visitMapper;
  private final ImageUrlResolver imageUrlResolver;

  public int countVisits(Long userId) {
    return visitMapper.countVisitsByUserId(userId);
  }

  public VisitProductListPageResponseDto findVisitProducts(
      VisitProductListSearchCondition condition) {
    int totalCount = visitMapper.countVisitsByUserId(condition.getUserId());
    int totalPages = Math.max((int) Math.ceil((double) totalCount / condition.getSize()), 1);
    var products = visitMapper.findVisitProducts(condition);

    products.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));

    return new VisitProductListPageResponseDto(
        products, condition.getPage(), condition.getSize(), totalCount, totalPages);
  }

  @Transactional
  public void recordVisit(Long userId, Long productId) {
    visitMapper.upsertVisit(userId, productId);
  }
}
