package kr.or.hieating.auth.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupUserParam {

  private String email;
  private String password;
  private String name;
  private String gender;
  private LocalDate birth;
}
