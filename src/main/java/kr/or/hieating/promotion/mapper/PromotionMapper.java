package kr.or.hieating.promotion.mapper;

import java.util.List;
import kr.or.hieating.promotion.domain.Promotions;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromotionMapper {

  List<Promotions> findActivePromotions();
}
