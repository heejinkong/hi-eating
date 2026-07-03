package kr.or.hieating.visit.service;

import kr.or.hieating.visit.mapper.VisitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitService {

  private final VisitMapper visitMapper;

  public int countVisits(Long userId) {
    return visitMapper.countVisitsByUserId(userId);
  }

  @Transactional
  public void recordVisit(Long userId, Long productId) {
    visitMapper.upsertVisit(userId, productId);
  }
}
