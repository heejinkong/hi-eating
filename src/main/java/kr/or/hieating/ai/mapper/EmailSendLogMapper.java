package kr.or.hieating.ai.mapper;

import java.util.List;
import kr.or.hieating.ai.dto.TargetUserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailSendLogMapper {

  int insertPendingLogs(
      @Param("hotDealId") long hotDealId, @Param("targets") List<TargetUserDto> targets);
}
