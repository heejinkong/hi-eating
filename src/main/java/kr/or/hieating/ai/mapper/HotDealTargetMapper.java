package kr.or.hieating.ai.mapper;

import java.util.List;
import kr.or.hieating.ai.dto.HotDealInfoRow;
import kr.or.hieating.ai.dto.HotDealProductInfoDto;
import kr.or.hieating.ai.dto.UserProfileRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HotDealTargetMapper {

  List<Long> findCategoryIdsByHotDealId(@Param("hotDealId") long hotDealId);

  List<Long> findCandidateUserIds(
      @Param("categoryIds") List<Long> categoryIds, @Param("recentMonths") int recentMonths);

  List<UserProfileRow> findUserProfilesByIds(
      @Param("userIds") List<Long> userIds,
      @Param("categoryIds") List<Long> categoryIds,
      @Param("recentMonths") int recentMonths);

  HotDealInfoRow findHotDealInfo(@Param("hotDealId") long hotDealId);

  List<HotDealProductInfoDto> findHotDealProducts(@Param("hotDealId") long hotDealId);
}
