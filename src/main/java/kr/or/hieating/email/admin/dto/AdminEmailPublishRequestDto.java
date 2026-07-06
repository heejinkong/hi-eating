package kr.or.hieating.email.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEmailPublishRequestDto {

  private List<Long> emailDraftIds;
}
