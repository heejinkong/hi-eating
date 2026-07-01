package kr.or.hieating.product.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record ProductOption(Long id, Long productId, int stock, LocalDate expireDate) {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

  public String formattedExpireDate() {
    return expireDate.format(DATE_FORMAT);
  }
}
