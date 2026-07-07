package kr.or.hieating.ai.mapper;

import java.util.List;
import kr.or.hieating.ai.dto.GeneratedHotDealEmailDto;
import kr.or.hieating.ai.dto.HotDealEmailInfoRow;
import kr.or.hieating.ai.dto.HotDealEmailProductRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HotDealEmailContentMapper {

  HotDealEmailInfoRow findHotDealInfo(@Param("hotDealId") long hotDealId);

  List<HotDealEmailProductRow> findHotDealProducts(@Param("hotDealId") long hotDealId);

  GeneratedHotDealEmailDto findGeneratedContent(@Param("hotDealId") long hotDealId);

  int upsertGeneratedContent(
      @Param("hotDealId") long hotDealId,
      @Param("subject") String subject,
      @Param("content") String content);

  int applyContentToSendLogs(
      @Param("hotDealId") long hotDealId,
      @Param("subject") String subject,
      @Param("content") String content);

  int updateValidationResult(
      @Param("hotDealId") long hotDealId,
      @Param("status") String status,
      @Param("reason") String reason);

  int applyValidationToSendLogs(
      @Param("hotDealId") long hotDealId,
      @Param("status") String status,
      @Param("reason") String reason);

  List<Long> findApprovedEmailSendLogIds(@Param("hotDealId") long hotDealId);

  int countSendLogs(@Param("hotDealId") long hotDealId);

  int countSendLogsByValidationOutcome(
      @Param("hotDealId") long hotDealId, @Param("validationStatus") String validationStatus);
}
