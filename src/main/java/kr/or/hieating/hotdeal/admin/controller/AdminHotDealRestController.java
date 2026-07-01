package kr.or.hieating.hotdeal.admin.controller;

import jakarta.validation.Valid;
import kr.or.hieating.global.apiPayload.ApiResponse;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.hotdeal.admin.dto.HotDealCreateRequestDTO;
import kr.or.hieating.hotdeal.admin.service.AdminHotDealService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/hotdeals")
public class AdminHotDealRestController {

  private final AdminHotDealService adminHotDealService;

  @PostMapping
  public ApiResponse<Integer> createHotDeal(@RequestBody @Valid HotDealCreateRequestDTO request) {
    if (request.getProducts() == null || request.getProducts().isEmpty()) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }
    int newHotDealId = adminHotDealService.createHotDeal(request);
    return ApiResponse.onSuccess(newHotDealId);
  }
}
