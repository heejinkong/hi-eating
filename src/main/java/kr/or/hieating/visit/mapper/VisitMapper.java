package kr.or.hieating.visit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VisitMapper {

  int countVisitsByUserId(@Param("userId") Long userId);

  int upsertVisit(@Param("userId") Long userId, @Param("productId") Long productId);
}
