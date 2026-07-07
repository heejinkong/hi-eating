package kr.or.hieating.hotdeal.admin.service;

import java.time.LocalDate;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Component;

@Component
public class HotDealPricePolicy {

  public void validatePeriod(LocalDate startsAt, LocalDate endsAt, LocalDate today) {
    if (startsAt.isBefore(today)) {
      throw new GeneralException(ErrorStatus.INVALID_START_DATE);
    }

    if (endsAt.isBefore(startsAt)) {
      throw new GeneralException(ErrorStatus.INVALID_END_DATE);
    }
  }

  public String determineStatus(LocalDate startsAt, LocalDate today) {
    return startsAt.isAfter(today) ? "SCHEDULED" : "ACTIVE";
  }

  public int calculateHotDealPrice(int originalPrice, int discountRate) {
    double discountMultiplier = (100 - discountRate) / 100.0;
    int calculatedPrice = (int) (originalPrice * discountMultiplier);
    return (int) (Math.round(calculatedPrice / 10.0) * 10);
  }

  public int calculateDiscountRate(int originalPrice, Integer hotDealPrice) {
    if (originalPrice <= 0 || hotDealPrice == null) {
      return 0;
    }

    return 100 - (hotDealPrice * 100 / originalPrice);
  }
}
