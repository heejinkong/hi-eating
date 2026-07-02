package kr.or.hieating.auth.domain;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Users {
  private long id; // DB numbers(19,0) 타입으로  선언됨
  private String email;
  private String password;
  private String name;
  private String gender;
  private LocalDate birth;
}
