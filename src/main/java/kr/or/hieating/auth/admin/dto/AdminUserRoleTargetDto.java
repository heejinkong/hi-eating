package kr.or.hieating.auth.admin.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class AdminUserRoleTargetDto {

  private Long id;
  private String email;
  private String name;
  private String gender;
  private LocalDate birth;
}
