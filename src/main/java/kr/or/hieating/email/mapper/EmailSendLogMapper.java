package kr.or.hieating.email.mapper;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.email.dto.EmailDraftDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailSendLogMapper {

  List<EmailDraftDto> findAll();

  List<EmailDraftDto> findReviewRequiredDrafts();

  List<EmailDraftDto> findPublishReadyDrafts();

  Optional<EmailDraftDto> findById(@Param("id") Long id);

  int updateContentAndApprove(
      @Param("id") Long id, @Param("subject") String subject, @Param("content") String content);

  int updateStatus(
      @Param("id") Long id,
      @Param("status") String status,
      @Param("failureReason") String failureReason);
}
