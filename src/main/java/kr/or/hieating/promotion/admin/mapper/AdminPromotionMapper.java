package kr.or.hieating.promotion.admin.mapper;

import java.util.List;
import kr.or.hieating.promotion.domain.Promotions;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 프로모션 배너 테이블(promotions)에 접근하는 MyBatis Mapper 인터페이스입니다. */
@Mapper
public interface AdminPromotionMapper {

  List<Promotions> selectAllPromotions();

  Promotions selectPromotionById(int id);

  List<Promotions> selectPromotionsByIdsForUpdate(@Param("ids") List<Integer> ids);

  List<Integer> selectAllPromotionIdsForUpdate();

  int selectMaxDisplayOrder();

  void insertPromotion(Promotions promotion);

  void updatePromotion(Promotions promotion);

  void updateDisplayOrder(@Param("id") int id, @Param("displayOrder") int displayOrder);

  void moveDisplayOrdersToTemporaryRange();

  void deletePromotion(int id);
}
