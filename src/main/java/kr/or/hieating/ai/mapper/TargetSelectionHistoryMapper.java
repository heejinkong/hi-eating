package kr.or.hieating.ai.mapper;

import java.util.List;
import kr.or.hieating.ai.dto.TargetSelectionEvaluationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TargetSelectionHistoryMapper {

  int upsertEvaluations(
      @Param("hotDealId") long hotDealId,
      @Param("evaluations") List<TargetSelectionEvaluationDto> evaluations);
}
