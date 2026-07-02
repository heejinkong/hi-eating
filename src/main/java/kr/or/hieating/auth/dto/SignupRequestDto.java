package kr.or.hieating.auth.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class SignupRequestDto {

  private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

  private String emailLocal; // @앞 식별자
  private String emailDomain; // gmail.com
  private String password;
  private String name;
  private String gender;
  private String birth;

  public String getEmail() {
    if (isBlank(emailLocal)) {
      return null;
    }

    String trimmedLocal = emailLocal.trim();
    if (trimmedLocal.contains("@") || isBlank(emailDomain)) {
      return trimmedLocal;
    }

    return trimmedLocal + "@" + emailDomain.trim();
  }

  public LocalDate getBirthDate() {
    if (isBlank(birth)) {
      return null;
    }

    String normalizedBirth = birth.trim().replace("-", "");
    return LocalDate.parse(normalizedBirth, BASIC_DATE_FORMATTER);
  }

  public SignupUserParam toUserParam(String encodedPassword) {
    return new SignupUserParam(getEmail(), encodedPassword, name, gender, getBirthDate());
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
