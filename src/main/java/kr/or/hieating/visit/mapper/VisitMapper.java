package kr.or.hieating.visit.mapper;

import java.util.List;
import kr.or.hieating.visit.dto.VisitProductListItemResponseDto;
import kr.or.hieating.visit.dto.VisitProductListSearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VisitMapper {

  int countVisitsByUserId(@Param("userId") Long userId);

  List<VisitProductListItemResponseDto> findVisitProducts(
      VisitProductListSearchCondition condition);

  int upsertVisit(@Param("userId") Long userId, @Param("productId") Long productId);
}
